package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop override is in its life cycle. */
enum class DropOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A drop override. Its code is unique within a tenant. */
data class DropOverride(
    var code: String,
    var label: String,
    var checksum: String,
    var active: Boolean,
    var carrier: String,
    var weight: Double,
    var depot: String,
    var budget: Double,
    var description: String,
    var phone: String,
    var capacity: Int,
    var status: DropOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DropOverrideStatus): Boolean {
        when (status) {
            DropOverrideStatus.PROPOSED -> return next == DropOverrideStatus.REJECTED
            DropOverrideStatus.APPROVED -> {
                when (next) {
                    DropOverrideStatus.REJECTED, DropOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            DropOverrideStatus.REJECTED -> {
                when (next) {
                    DropOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropOverrideStatus) {
        if (!canMoveTo(next)) {
            error("drop override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netValue(): Double = round(budget * capacity.toDouble() * 100) / 100

    fun snapshot(): DropOverride {
        return DropOverride(
            code = code,
            label = label,
            checksum = checksum,
            active = active,
            carrier = carrier,
            weight = weight,
            depot = depot,
            budget = budget,
            description = description,
            phone = phone,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a drop override in status proposed. */
fun newDropOverride(code: String, label: String): DropOverride {
    return DropOverride(
        code = code,
        label = label,
        checksum = "",
        active = false,
        carrier = "",
        weight = 0.0,
        depot = "",
        budget = 0.0,
        description = "",
        phone = "",
        capacity = 0,
        status = DropOverrideStatus.PROPOSED,
    )
}
