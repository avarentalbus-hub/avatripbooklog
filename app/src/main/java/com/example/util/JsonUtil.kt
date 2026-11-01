package com.example.util

import com.example.data.model.StageData
import org.json.JSONArray
import org.json.JSONObject

object JsonUtil {
  fun serializeStages(stages: List<StageData>): String {
    val array = JSONArray()
    for (stage in stages) {
      val obj = JSONObject()
      obj.put("key", stage.key)
      obj.put("title", stage.title)
      if (stage.odometer != null) obj.put("odometer", stage.odometer)
      if (stage.photoPath != null) obj.put("photoPath", stage.photoPath)
      if (stage.latitude != null) obj.put("latitude", stage.latitude)
      if (stage.longitude != null) obj.put("longitude", stage.longitude)
      obj.put("timestamp", stage.timestamp)
      array.put(obj)
    }
    return array.toString()
  }

  fun deserializeStages(json: String?): List<StageData> {
    if (json.isNullOrBlank()) return emptyList()
    val list = mutableListOf<StageData>()
    try {
      val array = JSONArray(json)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          StageData(
            key = obj.optString("key", ""),
            title = obj.optString("title", ""),
            odometer = if (obj.has("odometer")) obj.optDouble("odometer") else null,
            photoPath = if (obj.has("photoPath")) obj.optString("photoPath") else null,
            latitude = if (obj.has("latitude")) obj.optDouble("latitude") else null,
            longitude = if (obj.has("longitude")) obj.optDouble("longitude") else null,
            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
          )
        )
      }
    } catch (e: Exception) {
      // Fallback
    }
    return list
  }
}
