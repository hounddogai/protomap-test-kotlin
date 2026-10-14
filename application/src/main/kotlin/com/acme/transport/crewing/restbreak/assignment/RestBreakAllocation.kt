package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break allocation is in its life cycle. */
enum class RestBreakAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a rest break allocation and its current status. */
data class RestBreakAllocation(
    var code: String,
    var title: String,
    var active: Boolean,
    var maximum: Double,
    var pallets: Int,
    var shiftCode: String,
    var graceDays: Int,
    var rate: Double,
    var name: String,
    var status: RestBreakAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakAllocationStatus): Boolean {
        when (status) {
            RestBreakAllocationStatus.NEW -> {
                when (next) {
                    RestBreakAllocationStatus.QUEUED -> return true
                }
                return false
            }
            RestBreakAllocationStatus.QUEUED -> return next == RestBreakAllocationStatus.FAILED
            RestBreakAllocationStatus.RUNNING -> return next == RestBreakAllocationStatus.FAILED
            RestBreakAllocationStatus.DONE -> {
                when (next) {
                    RestBreakAllocationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakAllocationStatus) {
        if (!canMoveTo(next)) {
            error("rest break allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedValue(): Double = round((maximum - graceDays.toDouble()) * 100) / 100

    fun snapshot(): RestBreakAllocation {
        return RestBreakAllocation(
            code = code,
            title = title,
            active = active,
            maximum = maximum,
            pallets = pallets,
            shiftCode = shiftCode,
            graceDays = graceDays,
            rate = rate,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rest break allocation in status new. */
fun newRestBreakAllocation(code: String, title: String): RestBreakAllocation {
    return RestBreakAllocation(
        code = code,
        title = title,
        active = false,
        maximum = 0.0,
        pallets = 0,
        shiftCode = "",
        graceDays = 0,
        rate = 0.0,
        name = "",
        status = RestBreakAllocationStatus.NEW,
    )
}
