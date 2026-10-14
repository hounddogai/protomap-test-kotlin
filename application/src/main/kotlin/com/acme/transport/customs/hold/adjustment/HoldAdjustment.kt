package com.acme.transport.customs.hold.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold adjustment records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold adjustment is in its life cycle. */
enum class HoldAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a hold adjustment and its current status. */
data class HoldAdjustment(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var ownerId: String,
    var phone: String,
    var routeCode: String,
    var rate: Double,
    var approved: Boolean,
    var windowDays: Int,
    var status: HoldAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldAdjustmentStatus): Boolean {
        when (status) {
            HoldAdjustmentStatus.CREATED -> {
                when (next) {
                    HoldAdjustmentStatus.PUBLISHED -> return true
                }
                return false
            }
            HoldAdjustmentStatus.VALIDATED -> return next == HoldAdjustmentStatus.RETIRED
            HoldAdjustmentStatus.PUBLISHED -> {
                when (next) {
                    HoldAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("hold adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round((windowDays.toDouble() + rate) * 100) / 100

    fun snapshot(): HoldAdjustment {
        return HoldAdjustment(
            code = code,
            label = label,
            attributes = attributes,
            ownerId = ownerId,
            phone = phone,
            routeCode = routeCode,
            rate = rate,
            approved = approved,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a hold adjustment in status created. */
fun newHoldAdjustment(code: String, label: String): HoldAdjustment {
    return HoldAdjustment(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        ownerId = "",
        phone = "",
        routeCode = "",
        rate = 0.0,
        approved = false,
        windowDays = 0,
        status = HoldAdjustmentStatus.CREATED,
    )
}
