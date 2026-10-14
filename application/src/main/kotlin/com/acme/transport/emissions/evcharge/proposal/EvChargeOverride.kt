package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge override is in its life cycle. */
enum class EvChargeOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A ev charge override. Its code is unique within a tenant. */
data class EvChargeOverride(
    var code: String,
    var label: String,
    var archived: Boolean,
    var durationMinutes: Int,
    var endsAt: Int,
    var budget: Double,
    var region: String,
    var country: String,
    var status: EvChargeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeOverrideStatus): Boolean {
        when (status) {
            EvChargeOverrideStatus.NEW -> return next == EvChargeOverrideStatus.DONE || next == EvChargeOverrideStatus.FAILED
            EvChargeOverrideStatus.QUEUED -> return next == EvChargeOverrideStatus.DONE || next == EvChargeOverrideStatus.FAILED
            EvChargeOverrideStatus.RUNNING -> return next == EvChargeOverrideStatus.DONE
            EvChargeOverrideStatus.DONE -> {
                when (next) {
                    EvChargeOverrideStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("ev charge override $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"ev charge override"} $code"

    fun netVolume(): Double = round((budget + endsAt.toDouble()) * 100) / 100

    fun snapshot(): EvChargeOverride {
        return EvChargeOverride(
            code = code,
            label = label,
            archived = archived,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            budget = budget,
            region = region,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a ev charge override in status new. */
fun newEvChargeOverride(code: String, label: String): EvChargeOverride {
    return EvChargeOverride(
        code = code,
        label = label,
        archived = false,
        durationMinutes = 0,
        endsAt = 0,
        budget = 0.0,
        region = "",
        country = "",
        status = EvChargeOverrideStatus.NEW,
    )
}
