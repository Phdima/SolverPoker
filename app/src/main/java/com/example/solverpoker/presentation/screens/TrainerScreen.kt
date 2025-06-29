package com.example.solverpoker.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.solverpoker.presentation.components.PlayerProfile
import com.example.solverpoker.presentation.viewmodel.TrainerViewModel

@Composable
fun TrainerScreen(viewModel: TrainerViewModel = hiltViewModel()) {
    val gameState by viewModel.gameState

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Gray)
    ) {
        val tableWidth = maxWidth * 0.9f
        val tableHeight = maxHeight * 0.8f


        val playerSize = maxWidth * 0.4f

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(tableWidth, tableHeight)
                .padding(top = 30.dp)

        )
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
            {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(tableHeight),
                    Arrangement.SpaceEvenly
                ) {
                    PlayerProfile(
                        modifier = Modifier
                            .size(playerSize),
                        player = gameState.players[5]
                    )
                    PlayerProfile(
                        modifier = Modifier
                            .size(playerSize),
                        player = gameState.players[4]
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
                            .size(playerSize),
                        player = gameState.players[1]
                    )
                    PlayerProfile(
                        modifier = Modifier
                            .size(playerSize),
                        player = gameState.players[2]
                    )
                }
                PlayerProfile(
                    modifier = Modifier
                        .size(playerSize)
                        .align(Alignment.TopCenter),
                    player = gameState.players[0]
                )
                PlayerProfile(
                    modifier = Modifier
                        .size(playerSize)
                        .align(Alignment.BottomCenter),
                    player = gameState.players[3]
                )
            }

        }
        Button(
            onClick = { viewModel.startNewHand() },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) { Text("New Hand") }
    }
}
