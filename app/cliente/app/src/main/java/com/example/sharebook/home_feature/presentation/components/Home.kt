package com.example.sharebook.home_feature.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sharebook.R
import com.example.sharebook.core.presentation.components.book.BookPreview
import com.example.sharebook.core.presentation.components.FloatingButtonNewBook
import com.example.sharebook.core.presentation.navigation.routes.authenticated.PrivateRoutes
import com.example.sharebook.core.presentation.ui.theme.*
import com.example.sharebook.core.utils.UiText
import com.example.sharebook.home_feature.data.remote.inmemory.SectionsCarouselInMemory
import com.example.sharebook.home_feature.presentation.HomeViewModel

@Composable
fun Home(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val genders = listOf("Todos","Ação", "Aventura", "Ficção", "Romance", "Anime", "Terror", "Aventura")
    Surface(modifier = Modifier.fillMaxSize()) {
        FloatingButtonNewBook {}

        Column(modifier = Modifier
            .fillMaxSize()
            .background(background)
        ) {
            Header(homeViewModel, navController)
            Column(
                modifier = Modifier
                    .padding(16.dp, 8.dp, end = 4.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                LazyRow() {
                    items(genders) {
                        GenderRender(title = it)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                SectionCarousel(title = UiText.StringResource(R.string.home_carousel_available).asString()) {
                    LazyRow() {
                        items(SectionsCarouselInMemory.availableForExchange) {
                            BookPreview(
                                book = it,
                                onClick = {
                                    navController.navigate(PrivateRoutes.ExternalBook.route)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }


                Spacer(modifier = Modifier.height(20.dp))
                SectionCarousel(title = UiText.StringResource(R.string.home_carousel_nearToYou).asString()) {
                    LazyRow() {
                        items(SectionsCarouselInMemory.nearToYou) {
                            BookPreview(
                                book = it,
                                onClick = {
                                    navController.navigate(PrivateRoutes.ExternalBook.route)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(132.dp))
            }
        }
    }
}