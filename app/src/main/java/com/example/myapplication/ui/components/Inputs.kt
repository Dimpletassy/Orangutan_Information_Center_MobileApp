package com.example.myapplication.ui.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.palette.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Text(label, color = Cocoa, fontWeight = FontWeight.SemiBold)
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        shape = Pill,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        placeholder = {
            if (placeholder != null) Text(placeholder, color = Cocoa.copy(alpha = 0.6f))
        },
        leadingIcon = leadingIcon?.let { icon ->
            { Icon(icon, contentDescription = null, tint = Cocoa) }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Latte,
            unfocusedContainerColor = Latte,
            disabledContainerColor = Latte,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Cocoa,
            focusedTextColor = CocoaDeep,
            unfocusedTextColor = CocoaDeep
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    )
}

@Composable
fun FilledButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = Pill,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = Color.White),
        modifier = modifier.height(46.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}
