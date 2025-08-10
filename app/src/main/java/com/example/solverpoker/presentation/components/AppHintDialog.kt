package com.example.solverpoker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun AppHintDialog(onDismiss: () -> Unit, modifier: Modifier) {
    val textColor = MaterialTheme.colorScheme.onPrimary

    Box(modifier = modifier
        .fillMaxWidth()
        .padding(20.dp)
        .background(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(10.dp)
        )
        .border(
            width = 2.dp,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        )
        .clickable { onDismiss() }) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Добро пожаловать в тренажер для покерных стратегий!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
            )
            Column(modifier = Modifier.padding(start = 16.dp, bottom = 24.dp))
            {
                Text(
                    "• Чарты открытий и защиты на префлопе",
                    color = textColor
                )
                Text(
                    "• Симуляция CASH NLHE 6-max столов ",
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = textColor
                )
            }

            Text(
                text = "Новичкам рекомендуем самостоятельно:",
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(modifier = Modifier.padding(start = 24.dp, bottom = 24.dp)) {
                Text("1. Изучить покерные комбинации", color = textColor)
                Text("2. Освоить названия позиций за столом", color = textColor)
                Text("3. Разобрать стандартные действия", color = textColor)
            }

            Text(
                text = "Фокус приложения:",
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Тренировка префлоп-решений в динамичных условиях 6-max игры",
                color = textColor,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                "„Правильные решения на префлопе определяют 80% вашего успеха в NLHE“",
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )


        }

    }
}