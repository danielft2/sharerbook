package com.example.sharebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sharebook.ui.login.Login
import com.example.sharebook.ui.cadastro.Cadastro
import com.example.sharebook.ui.cadastro.Genero
import com.example.sharebook.ui.theme.SharebookTheme
import com.example.sharebook.ui.theme.gray200
import com.example.sharebook.ui.theme.green900

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SharebookTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Genero()
                }
            }
        }
    }
}

@Composable
fun Greeting() {
   Column(modifier = Modifier
       .fillMaxSize()
       .background(gray200)) {
       Text(
           text = "Criar nova conta.",
           color = green900,
           style = MaterialTheme.typography.h1
       )
   }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SharebookTheme {
        Genero()
    }
}