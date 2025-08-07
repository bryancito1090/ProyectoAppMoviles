package com.example.myapplication111

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    var textAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
        textAlpha = 1f
        delay(2000) // tiempo total visible del splash
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF232946), Color(0xFF0099FF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Círculo con gradiente oscuro para contraste
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF14213D), Color(0xFF232946)),
                            radius = 120f
                        ),
                        CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(R.drawable.buho),
                    contentDescription = "Logo EPN",
                    modifier = Modifier
                        .scale(scale.value)
                        .size(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "EPN Chat",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White.copy(alpha = textAlpha),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Asistente oficial para estudiantes",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = textAlpha),
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Loader animado
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 4.dp,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
