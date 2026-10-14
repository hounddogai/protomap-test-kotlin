package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered capacity group is in its life cycle. */
enum class TieredCapacityGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered capacity group. Its code is unique within a tenant. */
data class TieredCapacityGroup(
    var code: String,
    var name: String,
    var timezone: String,
    var notes: String,
    var quantity: Int,
    var budget: Double,
    var status: TieredCapacityGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCapacityGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCapacityGroupStatus): Boolean {
        when (status) {
            TieredCapacityGroupStatus.NEW -> return next == TieredCapacityGroupStatus.RUNNING
            TieredCapacityGroupStatus.QUEUED -> {
                when (next) {
                    TieredCapacityGroupStatus.FAILED -> return true
                }
                return false
            }
            TieredCapacityGroupStatus.RUNNING -> {
                when (next) {
                    TieredCapacityGroupStatus.DONE -> return true
                }
                return false
            }
            TieredCapacityGroupStatus.DONE -> {
                when (next) {
                    TieredCapacityGroupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCapacityGroupStatus) {
        if (!canMoveTo(next)) {
            error("tiered capacity group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((budget + quantity.toDouble()) * 100) / 100

    fun snapshot(): TieredCapacityGroup {
        return TieredCapacityGroup(
            code = code,
            name = name,
            timezone = timezone,
            notes = notes,
            quantity = quantity,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered capacity group in status new. */
fun newTieredCapacityGroup(code: String, name: String): TieredCapacityGroup {
    return TieredCapacityGroup(
        code = code,
        name = name,
        timezone = "",
        notes = "",
        quantity = 0,
        budget = 0.0,
        status = TieredCapacityGroupStatus.NEW,
    )
}
