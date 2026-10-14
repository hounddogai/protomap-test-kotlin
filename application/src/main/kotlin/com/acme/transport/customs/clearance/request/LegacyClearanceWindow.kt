package com.acme.transport.customs.clearance.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy clearance window is in its life cycle. */
enum class LegacyClearanceWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy clearance window. Its code is unique within a tenant. */
data class LegacyClearanceWindow(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var maximum: Double,
    var driverId: String,
    var attributes: MutableMap<String, String>,
    var status: LegacyClearanceWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyClearanceWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyClearanceWindowStatus): Boolean {
        when (status) {
            LegacyClearanceWindowStatus.NEW -> return next == LegacyClearanceWindowStatus.QUEUED
            LegacyClearanceWindowStatus.QUEUED -> {
                when (next) {
                    LegacyClearanceWindowStatus.FAILED -> return true
                }
                return false
            }
            LegacyClearanceWindowStatus.RUNNING -> {
                when (next) {
                    LegacyClearanceWindowStatus.FAILED -> return true
                }
                return false
            }
            LegacyClearanceWindowStatus.DONE -> {
                when (next) {
                    LegacyClearanceWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyClearanceWindowStatus) {
        if (!canMoveTo(next)) {
            error("legacy clearance window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((maximum + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyClearanceWindow {
        return LegacyClearanceWindow(
            code = code,
            label = label,
            updatedAt = updatedAt,
            maximum = maximum,
            driverId = driverId,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy clearance window in status new. */
fun newLegacyClearanceWindow(code: String, label: String): LegacyClearanceWindow {
    return LegacyClearanceWindow(
        code = code,
        label = label,
        updatedAt = 0,
        maximum = 0.0,
        driverId = "",
        attributes = mutableMapOf<String, String>(),
        status = LegacyClearanceWindowStatus.NEW,
    )
}
