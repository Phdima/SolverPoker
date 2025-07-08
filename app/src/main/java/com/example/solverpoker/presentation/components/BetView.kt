package com.example.solverpoker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solverpoker.domain.pokerLogic.Action
import com.example.solverpoker.domain.pokerLogic.Player

@Composable
fun BetView(modifier: Modifier = Modifier, player: Player) {


    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (!player.isHero)
            Text(

                text = player.action.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
    }
}