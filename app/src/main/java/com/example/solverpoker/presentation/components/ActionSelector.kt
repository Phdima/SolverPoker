package com.example.solverpoker.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.solverpoker.domain.pokerLogic.Action
import com.example.solverpoker.domain.pokerLogic.DefenseAction

@Composable
fun ActionSelector(
    selectedAction: DefenseAction,
    validActions: List<DefenseAction>,
    onActionSelected: (DefenseAction) -> Unit
) {
    Column {
        Text(
            text = "Выберите действие:",
            style = MaterialTheme.typography.titleMedium,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(validActions) { action ->
                Button(
                    onClick = { onActionSelected(action) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (action == selectedAction)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(text = action.name)
                }
            }
        }
    }
}