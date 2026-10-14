package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.text.normalize

/** Where a hold preference is in its life cycle. */
enum class HoldPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A hold preference as the hold team keeps it. */
data class HoldPreference(
    var code: String,
    var name: String,
    var source: String,
    var region: String,
    var fuelLiters: Double,
    var trailerId: String,
    var status: HoldPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldPreferenceStatus): Boolean {
        when (status) {
            HoldPreferenceStatus.NEW -> return next == HoldPreferenceStatus.DONE || next == HoldPreferenceStatus.FAILED
            HoldPreferenceStatus.QUEUED -> {
                when (next) {
                    HoldPreferenceStatus.RUNNING, HoldPreferenceStatus.FAILED -> return true
                }
                return false
            }
            HoldPreferenceStatus.RUNNING -> return next == HoldPreferenceStatus.DONE || next == HoldPreferenceStatus.FAILED
            HoldPreferenceStatus.DONE -> {
                when (next) {
                    HoldPreferenceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("hold preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): HoldPreference {
        return HoldPreference(
            code = code,
            name = name,
            source = source,
            region = region,
            fuelLiters = fuelLiters,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a hold preference in status new. */
fun newHoldPreference(code: String, name: String): HoldPreference {
    return HoldPreference(
        code = code,
        name = name,
        source = "",
        region = "",
        fuelLiters = 0.0,
        trailerId = "",
        status = HoldPreferenceStatus.NEW,
    )
}
