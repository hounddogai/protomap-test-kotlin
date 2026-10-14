package com.acme.transport.safety.violation.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation variance records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation variance is in its life cycle. */
enum class ViolationVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One violation variance, with what the planners need to act on it. */
data class ViolationVariance(
    var code: String,
    var name: String,
    var priority: Int,
    var carrier: String,
    var sequence: Int,
    var status: ViolationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationVarianceStatus): Boolean {
        when (status) {
            ViolationVarianceStatus.NEW -> {
                when (next) {
                    ViolationVarianceStatus.QUEUED, ViolationVarianceStatus.DONE -> return true
                }
                return false
            }
            ViolationVarianceStatus.QUEUED -> return next == ViolationVarianceStatus.DONE || next == ViolationVarianceStatus.FAILED
            ViolationVarianceStatus.RUNNING -> {
                when (next) {
                    ViolationVarianceStatus.FAILED -> return true
                }
                return false
            }
            ViolationVarianceStatus.DONE -> {
                when (next) {
                    ViolationVarianceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("violation variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveLoad(): Double = round((sequence.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): ViolationVariance {
        return ViolationVariance(
            code = code,
            name = name,
            priority = priority,
            carrier = carrier,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a violation variance in status new. */
fun newViolationVariance(code: String, name: String): ViolationVariance {
    return ViolationVariance(
        code = code,
        name = name,
        priority = 0,
        carrier = "",
        sequence = 0,
        status = ViolationVarianceStatus.NEW,
    )
}
