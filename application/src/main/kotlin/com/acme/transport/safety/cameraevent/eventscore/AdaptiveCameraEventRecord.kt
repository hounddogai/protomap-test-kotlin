package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive camera event record is in its life cycle. */
enum class AdaptiveCameraEventRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive camera event record. Its code is unique within a tenant. */
data class AdaptiveCameraEventRecord(
    var code: String,
    var title: String,
    var shiftCode: String,
    var minimum: Double,
    var attributes: MutableMap<String, String>,
    var quantity: Int,
    var phone: String,
    var dock: String,
    var country: String,
    var endsAt: Int,
    var status: AdaptiveCameraEventRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCameraEventRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCameraEventRecordStatus): Boolean {
        when (status) {
            AdaptiveCameraEventRecordStatus.NEW -> return next == AdaptiveCameraEventRecordStatus.RUNNING || next == AdaptiveCameraEventRecordStatus.FAILED
            AdaptiveCameraEventRecordStatus.QUEUED -> {
                when (next) {
                    AdaptiveCameraEventRecordStatus.DONE -> return true
                }
                return false
            }
            AdaptiveCameraEventRecordStatus.RUNNING -> {
                when (next) {
                    AdaptiveCameraEventRecordStatus.DONE, AdaptiveCameraEventRecordStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveCameraEventRecordStatus.DONE -> return next == AdaptiveCameraEventRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveCameraEventRecordStatus) {
        if (!canMoveTo(next)) {
            error("adaptive camera event record $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive camera event record"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((quantity.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveCameraEventRecord {
        return AdaptiveCameraEventRecord(
            code = code,
            title = title,
            shiftCode = shiftCode,
            minimum = minimum,
            attributes = attributes,
            quantity = quantity,
            phone = phone,
            dock = dock,
            country = country,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive camera event record in status new. */
fun newAdaptiveCameraEventRecord(code: String, title: String): AdaptiveCameraEventRecord {
    return AdaptiveCameraEventRecord(
        code = code,
        title = title,
        shiftCode = "",
        minimum = 0.0,
        attributes = mutableMapOf<String, String>(),
        quantity = 0,
        phone = "",
        dock = "",
        country = "",
        endsAt = 0,
        status = AdaptiveCameraEventRecordStatus.NEW,
    )
}
