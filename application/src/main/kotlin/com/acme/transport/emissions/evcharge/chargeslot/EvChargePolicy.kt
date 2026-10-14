package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge policy is in its life cycle. */
enum class EvChargePolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A ev charge policy as the ev charge team keeps it. */
data class EvChargePolicy(
    var code: String,
    var name: String,
    var endsAt: Int,
    var trailerId: String,
    var floorValue: Double,
    var minimum: Double,
    var amount: Double,
    var status: EvChargePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargePolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargePolicyStatus): Boolean {
        when (status) {
            EvChargePolicyStatus.DRAFT -> {
                when (next) {
                    EvChargePolicyStatus.ACTIVE, EvChargePolicyStatus.CLOSED -> return true
                }
                return false
            }
            EvChargePolicyStatus.PENDING -> {
                when (next) {
                    EvChargePolicyStatus.SUSPENDED, EvChargePolicyStatus.CLOSED -> return true
                }
                return false
            }
            EvChargePolicyStatus.ACTIVE -> {
                when (next) {
                    EvChargePolicyStatus.SUSPENDED -> return true
                }
                return false
            }
            EvChargePolicyStatus.SUSPENDED -> return next == EvChargePolicyStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: EvChargePolicyStatus) {
        if (!canMoveTo(next)) {
            error("ev charge policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"ev charge policy"} $code"

    fun netVolume(): Double = round(endsAt.toDouble() * floorValue * 100) / 100

    fun snapshot(): EvChargePolicy {
        return EvChargePolicy(
            code = code,
            name = name,
            endsAt = endsAt,
            trailerId = trailerId,
            floorValue = floorValue,
            minimum = minimum,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a ev charge policy in status draft. */
fun newEvChargePolicy(code: String, name: String): EvChargePolicy {
    return EvChargePolicy(
        code = code,
        name = name,
        endsAt = 0,
        trailerId = "",
        floorValue = 0.0,
        minimum = 0.0,
        amount = 0.0,
        status = EvChargePolicyStatus.DRAFT,
    )
}
