package net.j2i.webcrawler.data
import kotlinx.serialization.Serializable

@Serializable
data class UrlReading(val sessionID:Long=0L, val pageRequestID:Long = 0L, val url:String = "", val timestamp:Long = -1L) {
}