package com.example.sharebook.book_feature.presentation.external_book.components

import com.example.sharebook.book_feature.presentation.external_book.ExternalBookViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sharebook.R
import com.example.sharebook.book_feature.data.mock.model.BookDatailsMock
import com.example.sharebook.book_feature.presentation.external_book.event.SendRequestBookEvent
import com.example.sharebook.core.presentation.components.*
import com.example.sharebook.core.presentation.ui.theme.*
import com.example.sharebook.core.utils.UiText

@Composable
fun ExternalBook(
    viewModel: ExternalBookViewModel = hiltViewModel(),
    navController: NavController
) {
    val bookDatails = BookDatailsMock()
    Surface(modifier = Modifier.fillMaxSize()) {
        Column (modifier = Modifier
            .background(white)
            .fillMaxSize()
        ) {
            HeaderWithBackground {
                IconButtonAction(
                    resource = R.drawable.icon_arrow_back,
                    sizeValue = 28,
                    modifier = Modifier.background(white)
                ) { navController.popBackStack() }

                Text(
                    text = UiText.StringResource(R.string.book_details_header).asString(),
                    fontFamily = Lato,
                    fontWeight = FontWeight.Bold,
                    color = white,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(2f)
                        .padding(end = 44.dp),
                )
            }

            Column(modifier = Modifier
                .padding(top = 0.dp, start = 16.dp, bottom = 16.dp, end = 16.dp)
                .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                )  {
                    Spacer(modifier = Modifier.height(16.dp))

                    BookInformations()

                    DividerCustom(spaceTop = 20.dp, spaceBottom = 20.dp)

                    Column {
                        Text(
                            text = UiText.StringResource(R.string.book_datails_descreption_session).asString(),
                            fontFamily = FontFamily(Font(R.font.lato_bold)),
                            fontSize = 15.sp,
                            color = green900,
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        TextButtonMore(text = bookDatails.descricao)
                    }

                    DividerCustom(spaceTop = 20.dp, spaceBottom = 20.dp)
                    BoolGaleryImage()
                }

                Spacer(modifier = Modifier.height(16.dp))
                ButtonPrimary(
                    text = UiText.StringResource(R.string.book_datails_request_book_button).asString(),
                    onClick = { viewModel.onEvent(SendRequestBookEvent.SendRequest) }
                )
            }
        }
    }
}