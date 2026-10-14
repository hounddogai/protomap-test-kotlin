package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual fuel surcharge segment is in its life cycle. */
enum class ManualFuelSurchargeSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual fuel surcharge segment as the fuel surcharge team keeps it. */
data class ManualFuelSurchargeSegment(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var minimum: Double,
    var stopCount: Int,
    var expiresAt: Int,
    var channel: String,
    var loadKg: Double,
    var routeCode: String,
    var status: ManualFuelSurchargeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualFuelSurchargeSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualFuelSurchargeSegmentStatus): Boolean {
        when (status) {
            ManualFuelSurchargeSegmentStatus.NEW -> return next == ManualFuelSurchargeSegmentStatus.RUNNING
            ManualFuelSurchargeSegmentStatus.QUEUED -> {
                when (next) {
                    ManualFuelSurchargeSegmentStatus.RUNNING, ManualFuelSurchargeSegmentStatus.FAILED -> return true
                }
                return false
            }
            ManualFuelSurchargeSegmentStatus.RUNNING -> return next == ManualFuelSurchargeSegmentStatus.DONE || next == ManualFuelSurchargeSegmentStatus.FAILED
            ManualFuelSurchargeSegmentStatus.DONE -> {
                when (next) {
                    ManualFuelSurchargeSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualFuelSurchargeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("manual fuel surcharge segment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((minimum + stopCount.toDouble()) * 100) / 100

    fun snapshot(): ManualFuelSurchargeSegment {
        return ManualFuelSurchargeSegment(
            code = code,
            title = title,
            attributes = attributes,
            minimum = minimum,
            stopCount = stopCount,
            expiresAt = expiresAt,
            channel = channel,
            loadKg = loadKg,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a manual fuel surcharge segment in status new. */
fun newManualFuelSurchargeSegment(code: String, title: String): ManualFuelSurchargeSegment {
    return ManualFuelSurchargeSegment(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        minimum = 0.0,
        stopCount = 0,
        expiresAt = 0,
        channel = "",
        loadKg = 0.0,
        routeCode = "",
        status = ManualFuelSurchargeSegmentStatus.NEW,
    )
}
