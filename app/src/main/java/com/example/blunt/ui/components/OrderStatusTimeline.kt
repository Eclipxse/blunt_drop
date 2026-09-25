package com.example.blunt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blunt.model.OrderStatus

@Composable fun OrderStatusTimeline(status: OrderStatus) {
    if (status == OrderStatus.CANCELLED) { Text("Order cancelled. No real payment was taken.", color = MaterialTheme.colorScheme.onSurfaceVariant); return }
    Column {
        OrderStatus.entries.filter { it != OrderStatus.CANCELLED }.forEachIndexed { index, step ->
            val reached = index <= status.ordinal
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(28.dp).background(if (reached) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
                        if (reached) Icon(Icons.Outlined.Check, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onTertiary)
                    }
                    if (step != OrderStatus.DELIVERED) Box(Modifier.width(2.dp).height(30.dp).background(if (index < status.ordinal) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant))
                }
                Column(Modifier.padding(top = 3.dp)) { Text(step.label, style = MaterialTheme.typography.titleSmall, color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant); if (step == status) Text("Current status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary) }
            }
        }
    }
}
