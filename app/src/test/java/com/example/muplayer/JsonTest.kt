package com.example.muplayer

import org.json.JSONArray
import org.junit.Test

class JsonTest {
    @Test
    fun testJson() {
        val arr = JSONArray("[{\"id\":\"1\", \"tracks\":[]}]")
        val obj = arr.getJSONObject(0)
        val tracks = obj.getJSONArray("tracks")
        tracks.put(123L)
        println(arr.toString())
    }
}
