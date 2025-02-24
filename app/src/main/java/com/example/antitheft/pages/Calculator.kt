package com.example.antitheft.pages

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.io.File
import java.util.Stack

@Composable
fun Calculator(navController: NavHostController, context: Context) {
    // States to store user input and results
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    // Function to handle button clicks
    fun onButtonClick(value: String) {
        when (value) {
            "AC" -> {
                input = ""
                result = ""
            }

            "=" -> {
                try {
                    // Evaluate the result using a simple Kotlin expression evaluator
                    val evalResult = evaluateExpression(input)
                    result = evalResult.toString()

                    // Check if the result matches the stored PIN
                    val dcimDir = File(context.getExternalFilesDir(null), "SentinelX/Pin").apply {
                        parentFile?.mkdirs() // Ensure parent directories exist
                    }
                    val pinFile = File(dcimDir, "pin.txt")
                    val storedPin = pinFile.readText().trim()
                    if (input == storedPin) {
                        navController.navigate("splash_screen")
                    }
                } catch (e: Exception) {
                    result = "Error"
                }
            }

            else -> input += value
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Display input and result at the top
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = input,
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
                    .offset(y=25.dp)
            )
            Text(
                text = result,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.inversePrimary,
                fontWeight = FontWeight.Light,
                modifier = Modifier.offset(y = 8.dp)
            )
        }

        // Spacer to push the buttons to the bottom
        Spacer(modifier = Modifier.weight(1f))

        // Calculator buttons
        val buttons = listOf(
            listOf("AC", "+/-", "%", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", "."," ", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { label ->
                    Button(
                        onClick = { onButtonClick(label) },
                        modifier = Modifier
                            .weight(if (label == "=") 1f else 1f) // Double weight for 0 button
                            .aspectRatio(1f)
                            .padding(8.dp),
                        shape = MaterialTheme.shapes.medium.copy(CornerSize(16.dp)),
                        colors = androidx.compose.material.ButtonDefaults.buttonColors(
                            backgroundColor = when {
                                label in listOf(
                                    "/",
                                    "*",
                                    "-",
                                    "+",
                                    "="
                                ) -> Color(0xFFFF9800) // Orange for operators
                                label == "AC" -> Color(0xFFB0BEC5) // Light grey for AC
                                else -> MaterialTheme.colorScheme.primary // Default color for numbers
                            }
                        ),
                    ) {
                        Text(
                            text = label,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Helper function to evaluate mathematical expressions

fun evaluateExpression(expression: String): Double {
    val sanitizedExpression = expression.replace("x", "*").replace("\u00F7", "/")
    val tokens = sanitizedExpression.toCharArray()

    val values: Stack<Double> = Stack()
    val operators: Stack<Char> = Stack()

    var i = 0
    while (i < tokens.size) {
        if (tokens[i].isWhitespace()) {
            i++
            continue
        }

        // Number
        if (tokens[i].isDigit()) {
            val buffer = StringBuilder()
            while (i < tokens.size && (tokens[i].isDigit() || tokens[i] == '.')) {
                buffer.append(tokens[i++])
            }
            values.push(buffer.toString().toDouble())
            i-- // Adjust for next iteration
        }
        // Open Parenthesis
        else if (tokens[i] == '(') {
            operators.push(tokens[i])
        }
        // Close Parenthesis
        else if (tokens[i] == ')') {
            while (operators.peek() != '(') {
                values.push(applyOperator(operators.pop(), values.pop(), values.pop()))
            }
            operators.pop()
        }
        // Operator
        else if (tokens[i] in "+-*/") {
            while (operators.isNotEmpty() && precedence(tokens[i]) <= precedence(operators.peek())) {
                values.push(applyOperator(operators.pop(), values.pop(), values.pop()))
            }
            operators.push(tokens[i])
        }
        i++
    }

    while (operators.isNotEmpty()) {
        values.push(applyOperator(operators.pop(), values.pop(), values.pop()))
    }

    return values.pop()
}

fun precedence(op: Char): Int {
    return when (op) {
        '+', '-' -> 1
        '*', '/' -> 2
        else -> -1
    }
}

fun applyOperator(op: Char, b: Double, a: Double): Double {
    return when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> a / b
        else -> throw UnsupportedOperationException("Invalid operator")
    }
}