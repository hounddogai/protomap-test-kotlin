package com.acme.transport.safety.cameraevent.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict camera event tier records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a strict camera event tier is in its life cycle. */
enum class StrictCameraEventTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a strict camera event tier and its current status. */
data class StrictCameraEventTier(
    var code: String,
    var name: String,
    var visible: Boolean,
    var rate: Double,
    var summary: String,
    var discount: Double,
    var category: String,
    var stopCount: Int,
    var weight: Double,
    var status: StrictCameraEventTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCameraEventTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCameraEventTierStatus): Boolean {
        when (status) {
            StrictCameraEventTierStatus.NEW -> return next == StrictCameraEventTierStatus.QUEUED
            StrictCameraEventTierStatus.QUEUED -> {
                when (next) {
                    StrictCameraEventTierStatus.DONE, StrictCameraEventTierStatus.FAILED -> return true
                }
                return false
            }
            StrictCameraEventTierStatus.RUNNING -> return next == StrictCameraEventTierStatus.DONE || next == StrictCameraEventTierStatus.FAILED
            StrictCameraEventTierStatus.DONE -> return next == StrictCameraEventTierStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictCameraEventTierStatus) {
        if (!canMoveTo(next)) {
            error("strict camera event tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round((discount + stopCount.toDouble()) * 100) / 100

    fun snapshot(): StrictCameraEventTier {
        return StrictCameraEventTier(
            code = code,
            name = name,
            visible = visible,
            rate = rate,
            summary = summary,
            discount = discount,
            category = category,
            stopCount = stopCount,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a strict camera event tier in status new. */
fun newStrictCameraEventTier(code: String, name: String): StrictCameraEventTier {
    return StrictCameraEventTier(
        code = code,
        name = name,
        visible = false,
        rate = 0.0,
        summary = "",
        discount = 0.0,
        category = "",
        stopCount = 0,
        weight = 0.0,
        status = StrictCameraEventTierStatus.NEW,
    )
}
