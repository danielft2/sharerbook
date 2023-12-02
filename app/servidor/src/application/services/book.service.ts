/* eslint-disable prettier/prettier */
import { Injectable, NotFoundException } from '@nestjs/common';
import { BookRepository } from '../../infraestructure/repositories/book.repository';
import { IbgeFinderService } from './ibge-finder.service';
import { UserRepository } from '../../infraestructure/repositories/user.repository';
import { UserGendersService } from './user-gender.service';
import { BookGendersService } from './book-gender.service';
import { BookStateService } from './book-state.service';
import { RescueService } from './rescue.service';
import { Book } from '../../domain/entities/book.entity';
import { SupabaseService } from './supabase.service';
import { randomUUID } from 'crypto';

@Injectable()
export class BookService {
  constructor(
    private bookRepository: BookRepository,
    private ibgeFinderService: IbgeFinderService,
    private userRepository: UserRepository,
    private userGenderService: UserGendersService,
    private bookGenderService: BookGendersService,
    private bookStateService: BookStateService,
    private rescueService: RescueService,
    private supabaseService: SupabaseService,
  ) {}

  async getUserIBGE(user_cep: string) {
    return await this.ibgeFinderService.getIBGE(user_cep);
  }

  async getUserUf(user_cep: string) {
    return await this.ibgeFinderService.getUf(user_cep);
  }

  async containsGender(user_id: string, book_id: string) {
    const userGenders = await this.userGenderService.findAllByUserId(user_id);
    const bookGenders = await this.bookGenderService.findAllByBookId(book_id);

    return userGenders.some((userGenders) => bookGenders.includes(userGenders));
  }

  async getBookByISBN(isbn: string){
    const book = (await this.bookRepository.findMany()).find(
      book => book.isbn === isbn
    )
    return book;
  }

  async findUnique(id: string){
    return await this.bookRepository.findOne(id);
  }

  async detailedBook(book_id: string) {
    const book = await this.bookRepository.findOne(book_id);
    const bookOwner = await this.userRepository.findById(book.usuario_id);
  
    const userInformation = {
      profilePhoto: bookOwner.foto_perfil,
      userName: bookOwner.nome,
      city: bookOwner.cidade,
      uf: await this.getUserUf(bookOwner.cep),
    };
  
    let images = [];
  
    if (book.imagens && Array.isArray(book.imagens)) {
      images = await Promise.all(
        book.imagens
          .filter((image) => image !== '')
          .map((image) =>
            this.supabaseService.getFileURL(image, 'BookImages')
          )
      );
    }
  
    return {
      userInformation,
      book: {
        id: book.id,
        isbn: book.isbn,
        nome: book.nome,
        sinopse: book.sinopse,
        autor: book.autor,
        usuario_id: book.usuario_id,
        edicao: book.edicao,
        idioma: book.idioma,
        pode_buscar: book.pode_buscar,
        pode_receber: book.quer_receber,
        capa: await this.supabaseService.getFileURL(book.capa, 'BookImages'),
        imagens: images,
        genders: await this.bookGenderService.findGenderName(book.id),
        book_state: (
          await this.bookStateService.findOne(book.estado_id)
        ).nome,
      },
    };
  }

  async findAll(user_id: string) {
    const user = await this.userRepository.findById(user_id);
    const userUf = await this.getUserUf(user.cep);
  
    const books = await this.bookRepository.findMany();
    const filteredBooks = books.filter((book) => book.usuario_id !== user_id);
  
    const getFileURL = async (book: Book) => ({
      id: book.id,
      nome: book.nome,
      autores: book.autor,
      capa: await this.supabaseService.getFileURL(book.capa, 'BookImages'),
      latitude: book.latitude,
      longitude: book.longitude
    });
  
    const [availableBooks, favoriteGenders, nextToYou] = await Promise.all([
      Promise.all(filteredBooks.map(getFileURL)),
      Promise.all(
        filteredBooks
          .filter((book) => this.containsGender(user_id, book.id))
          .map(getFileURL)
      ),
      Promise.all(
        filteredBooks
          .filter(async (book) => {
            const userNextToYou = await this.userRepository.findById(
              book.usuario_id
            );
            const userNextToYouUf = await this.getUserUf(userNextToYou.cep);
            return userUf === userNextToYouUf;
          })
          .map(getFileURL)
      ),
    ]);
  
    return { availableBooks, favoriteGenders, nextToYou };
  }

  async findMyBooks(user_id: string) {
    const books = await this.bookRepository.findMany();

    const myBooks = await Promise.all(
      books
        .filter((book) => user_id === book.usuario_id)
        .map(async (book) => ({
          id: book.id,
          nome: book.nome,
          autores: book.autor,
          capa: await this.supabaseService.getFileURL(book.capa, 'BookImages'),
          edicao: book.edicao,
          pode_receber: book.quer_receber,
          pode_enviar: book.pode_buscar,
          genero: await this.bookGenderService.findGenderName(book.id),
          estado: (await this.bookStateService.findOne(book.estado_id)).nome,
        })),
    );

    return myBooks;
  }

  async findOne(book_id: string, user_id: string) {
    const book = await this.bookRepository.findOne(book_id);
    const user = await this.userRepository.findById(user_id);
    const detailedBook = await this.detailedBook(book_id);
    if (user.id === book.usuario_id) {
      const rescueDataPromises = (await this.rescueService.findAll())
        .filter(rescue => rescue.livro_id === book_id)
        .map(async (rescue) => {
          const user = await this.userRepository.findById(rescue.usuario_solicitante_id);
          return {
            nome: user.nome,
            cidade: user.cidade,
            uf: await this.getUserUf(user.cep),
            foto_perfil: await this.supabaseService.getFileURL(user.nome, 'UserImages')
          };
        });
      const rescues = await Promise.all(rescueDataPromises);
      return {
        rescues,
        detailedBook,
        is_request: false,
      };
    } else {
      const is_request = await this.rescueService.findIfUserHasRequestedBook(book_id, user_id);
      return {
        detailedBook,
        is_request,
      };
    }
  }

  async create(book: Book, collection: Record<'cape' | 'images', Express.Multer.File[]>) {
    const cape = collection.cape.at(0).buffer;
    const capa = book.nome.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    this.supabaseService.create(capa, 'BookImages', cape);

    const imagesName: string[] = await Promise.all(
      collection.images.map(async (image) => {
        const imageName = randomUUID();
        await this.supabaseService.create(imageName, 'BookImages', image.buffer);
        return imageName;
      })
    );  
    return await this.bookRepository.create({
      ...book,
      capa: capa,
      imagens: imagesName
    });
  }

  async update(book: Book, cape: Express.Multer.File){
    const findedBook = await this.bookRepository.findOne(book.id);
    if(!findedBook.id) throw new NotFoundException();
    else if(await this.rescueService.findIfABookWasRequested(book.id)){
      throw new Error("Você não pode editar esse livro, ele ja foi solicitado")
    }
    else{
      const updatedCapeName = book.nome;
      this.supabaseService.remove(findedBook.nome, 'BookImages');
      this.supabaseService.create(updatedCapeName, 'BookImages', cape.buffer);
      return this.bookRepository.update({
        ...book,
        capa: updatedCapeName,
        imagens: findedBook.imagens
      });
    }
  }

  async delete(book_id: string){
    const book = await this.bookRepository.findOne(book_id);
    
    if(!book){
      throw new NotFoundException();
    } else if(await this.rescueService.findIfABookWasRequested(book_id)) {
      throw new Error("Você não pode excluir esse livro, ele ja foi solicitado")
    } else {
      this.bookRepository.delete(book_id);
      return true;
    }
  }
}
