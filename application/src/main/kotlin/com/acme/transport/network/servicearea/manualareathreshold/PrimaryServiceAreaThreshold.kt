package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary service area threshold records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a primary service area threshold is in its life cycle. */
enum class PrimaryServiceAreaThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a primary service area threshold and its current status. */
data class PrimaryServiceAreaThreshold(
    var code: String,
    var name: String,
    var windowDays: Int,
    var region: String,
    var checksum: String,
    var maximum: Double,
    var quantity: Int,
    var margin: Double,
    var status: PrimaryServiceAreaThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServiceAreaThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServiceAreaThresholdStatus): Boolean {
        when (status) {
            PrimaryServiceAreaThresholdStatus.NEW -> {
                when (next) {
                    PrimaryServiceAreaThresholdStatus.QUEUED, PrimaryServiceAreaThresholdStatus.FAILED -> return true
                }
                return false
            }
            PrimaryServiceAreaThresholdStatus.QUEUED -> {
                when (next) {
                    PrimaryServiceAreaThresholdStatus.DONE -> return true
                }
                return false
            }
            PrimaryServiceAreaThresholdStatus.RUNNING -> return next == PrimaryServiceAreaThresholdStatus.DONE
            PrimaryServiceAreaThresholdStatus.DONE -> return next == PrimaryServiceAreaThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryServiceAreaThresholdStatus) {
        if (!canMoveTo(next)) {
            error("primary service area threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round((maximum - quantity.toDouble()) * 100) / 100

    fun snapshot(): PrimaryServiceAreaThreshold {
        return PrimaryServiceAreaThreshold(
            code = code,
            name = name,
            windowDays = windowDays,
            region = region,
            checksum = checksum,
            maximum = maximum,
            quantity = quantity,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a primary service area threshold in status new. */
fun newPrimaryServiceAreaThreshold(code: String, name: String): PrimaryServiceAreaThreshold {
    return PrimaryServiceAreaThreshold(
        code = code,
        name = name,
        windowDays = 0,
        region = "",
        checksum = "",
        maximum = 0.0,
        quantity = 0,
        margin = 0.0,
        status = PrimaryServiceAreaThresholdStatus.NEW,
    )
}
