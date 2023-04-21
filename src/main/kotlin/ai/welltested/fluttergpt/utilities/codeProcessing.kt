package ai.welltested.fluttergpt.utilities

fun extractDartCode(widgetCode: String): String {
    val parts = widgetCode.split("```")
    if (parts.size >= 2) {
        val dartCode = parts[1]
        return if (dartCode.startsWith("dart")) {
            dartCode.substring(4)
        } else {
            dartCode
        }
    } else {
        return if (widgetCode.startsWith("dart")) {
            widgetCode.substring(4)
        } else {
            widgetCode
        }
    }
}

fun extractExplanation(widgetCode: String): String {
    val parts = widgetCode.split("```")
    if (parts.isNotEmpty()) {
        return parts[0]
    } else {
        return widgetCode
    }
}