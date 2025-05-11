// File: ui/theme/Shapes.kt
package com.example.personaltutorapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Defines the shape system for Material Design components in the application.
// These shapes are used in MaterialTheme.shapes for consistent rounded corners.
val Shapes = Shapes(
    // Small shape with 8.dp corner radius, used for compact components like buttons and chips.
    small = RoundedCornerShape(8.dp),
    // Medium shape with 12.dp corner radius, used for medium-sized components like cards and dialogs.
    medium = RoundedCornerShape(12.dp),
    // Large shape with 16.dp corner radius, used for large components like bottom sheets and navigation bars.
    large = RoundedCornerShape(16.dp)
)