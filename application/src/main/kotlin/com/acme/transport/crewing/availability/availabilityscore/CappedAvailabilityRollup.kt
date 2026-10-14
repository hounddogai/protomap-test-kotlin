package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped availability rollup is in its life cycle. */
enum class CappedAvailabilityRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a capped availability rollup and its current status. */
data class CappedAvailabilityRollup(
    var code: String,
    var name: String,
    var rate: Double,
    var trailerId: String,
    var pallets: Int,
    var margin: Double,
    var segment: String,
    var timezone: String,
    var driverId: String,
    var graceDays: Int,
    var threshold: Double,
    var status: CappedAvailabilityRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAvailabilityRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAvailabilityRollupStatus): Boolean {
        when (status) {
            CappedAvailabilityRollupStatus.NEW -> {
                when (next) {
                    CappedAvailabilityRollupStatus.DONE -> return true
                }
                return false
            }
            CappedAvailabilityRollupStatus.QUEUED -> return next == CappedAvailabilityRollupStatus.DONE
            CappedAvailabilityRollupStatus.RUNNING -> return next == CappedAvailabilityRollupStatus.DONE || next == CappedAvailabilityRollupStatus.FAILED
            CappedAvailabilityRollupStatus.DONE -> return next == CappedAvailabilityRollupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedAvailabilityRollupStatus) {
        if (!canMoveTo(next)) {
            error("capped availability rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round(margin * threshold * 100) / 100

    fun snapshot(): CappedAvailabilityRollup {
        return CappedAvailabilityRollup(
            code = code,
            name = name,
            rate = rate,
            trailerId = trailerId,
            pallets = pallets,
            margin = margin,
            segment = segment,
            timezone = timezone,
            driverId = driverId,
            graceDays = graceDays,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped availability rollup in status new. */
fun newCappedAvailabilityRollup(code: String, name: String): CappedAvailabilityRollup {
    return CappedAvailabilityRollup(
        code = code,
        name = name,
        rate = 0.0,
        trailerId = "",
        pallets = 0,
        margin = 0.0,
        segment = "",
        timezone = "",
        driverId = "",
        graceDays = 0,
        threshold = 0.0,
        status = CappedAvailabilityRollupStatus.NEW,
    )
}
