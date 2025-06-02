package com.example.solverpoker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TableView() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Gray)
    ) {
        val tableWidth = maxWidth * 0.8f
        val tableHeight = maxHeight * 0.6f


        val playerSize = maxWidth * 0.2f

        Box(Modifier.align(Alignment.Center).size(tableWidth, tableHeight))
        {
            Box(
                modifier = Modifier
                    .size(tableWidth, tableHeight)
                    .background(
                        color = Color(0xFF2E7D32),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 4.dp,
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(16.dp)
                    )

            )

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .height(tableHeight),
                Arrangement.SpaceEvenly
            ) {
                PlayerProfile(
                    modifier = Modifier
                        .size(playerSize)
                )
                PlayerProfile(
                    modifier = Modifier
                        .size(playerSize)
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(tableHeight),
                Arrangement.SpaceEvenly
            ) {
                PlayerProfile(
                    modifier = Modifier
                        .size(playerSize)
                )
                PlayerProfile(
                    modifier = Modifier
                        .size(playerSize)
                )
            }
            PlayerProfile(
                modifier = Modifier
                    .size(playerSize)
                    .align(Alignment.TopCenter)
            )
            PlayerProfile(
                modifier = Modifier
                    .size(playerSize)
                    .align(Alignment.BottomCenter)
            )
        }

    }
}
