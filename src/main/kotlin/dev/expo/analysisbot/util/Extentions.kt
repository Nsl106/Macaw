package dev.expo.analysisbot.util

import com.google.gson.JsonElement

fun JsonElement.get(memberName: String): JsonElement {
    if (isJsonPrimitive) return asJsonPrimitive.get(memberName)
    if (isJsonObject) return asJsonObject.get(memberName)
    if (isJsonArray) return asJsonArray.get(memberName)
    if (isJsonNull) return asJsonNull.get(memberName)
    return this
}

