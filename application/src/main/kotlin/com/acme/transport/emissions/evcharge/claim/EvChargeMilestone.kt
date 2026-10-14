package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge milestone is in its life cycle. */
enum class EvChargeMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A ev charge milestone. Its code is unique within a tenant. */
data class EvChargeMilestone(
    var code: String,
    var name: String,
    var ratio: Double,
    var archived: Boolean,
    var currency: String,
    var loadKg: Double,
    var depot: String,
    var fuelLiters: Double,
    var limit: Int,
    var status: EvChargeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeMilestoneStatus): Boolean {
        when (status) {
            EvChargeMilestoneStatus.IDLE -> {
                when (next) {
                    EvChargeMilestoneStatus.DRAINING, EvChargeMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            EvChargeMilestoneStatus.WARMING -> {
                when (next) {
                    EvChargeMilestoneStatus.DRAINING -> return true
                }
                return false
            }
            EvChargeMilestoneStatus.READY -> {
                when (next) {
                    EvChargeMilestoneStatus.DRAINING, EvChargeMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            EvChargeMilestoneStatus.DRAINING -> {
                when (next) {
                    EvChargeMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("ev charge milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round(ratio * loadKg * 100) / 100

    fun snapshot(): EvChargeMilestone {
        return EvChargeMilestone(
            code = code,
            name = name,
            ratio = ratio,
            archived = archived,
            currency = currency,
            loadKg = loadKg,
            depot = depot,
            fuelLiters = fuelLiters,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a ev charge milestone in status idle. */
fun newEvChargeMilestone(code: String, name: String): EvChargeMilestone {
    return EvChargeMilestone(
        code = code,
        name = name,
        ratio = 0.0,
        archived = false,
        currency = "",
        loadKg = 0.0,
        depot = "",
        fuelLiters = 0.0,
        limit = 0,
        status = EvChargeMilestoneStatus.IDLE,
    )
}
