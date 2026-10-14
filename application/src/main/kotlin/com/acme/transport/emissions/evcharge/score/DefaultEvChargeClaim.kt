package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default ev charge claim is in its life cycle. */
enum class DefaultEvChargeClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a default ev charge claim and its current status. */
data class DefaultEvChargeClaim(
    var code: String,
    var name: String,
    var channel: String,
    var fuelLiters: Double,
    var revision: Int,
    var lane: String,
    var summary: String,
    var label: String,
    var status: DefaultEvChargeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultEvChargeClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultEvChargeClaimStatus): Boolean {
        when (status) {
            DefaultEvChargeClaimStatus.NEW -> {
                when (next) {
                    DefaultEvChargeClaimStatus.DONE -> return true
                }
                return false
            }
            DefaultEvChargeClaimStatus.QUEUED -> {
                when (next) {
                    DefaultEvChargeClaimStatus.RUNNING, DefaultEvChargeClaimStatus.FAILED -> return true
                }
                return false
            }
            DefaultEvChargeClaimStatus.RUNNING -> return next == DefaultEvChargeClaimStatus.DONE || next == DefaultEvChargeClaimStatus.FAILED
            DefaultEvChargeClaimStatus.DONE -> {
                when (next) {
                    DefaultEvChargeClaimStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultEvChargeClaimStatus) {
        if (!canMoveTo(next)) {
            error("default ev charge claim $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($name)"

    fun effectiveTotal(): Double = round(revision.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): DefaultEvChargeClaim {
        return DefaultEvChargeClaim(
            code = code,
            name = name,
            channel = channel,
            fuelLiters = fuelLiters,
            revision = revision,
            lane = lane,
            summary = summary,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default ev charge claim in status new. */
fun newDefaultEvChargeClaim(code: String, name: String): DefaultEvChargeClaim {
    return DefaultEvChargeClaim(
        code = code,
        name = name,
        channel = "",
        fuelLiters = 0.0,
        revision = 0,
        lane = "",
        summary = "",
        label = "",
        status = DefaultEvChargeClaimStatus.NEW,
    )
}
