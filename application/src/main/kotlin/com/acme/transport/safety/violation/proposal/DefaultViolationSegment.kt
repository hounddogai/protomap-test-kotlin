package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.text.normalize

/** Where a default violation segment is in its life cycle. */
enum class DefaultViolationSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default violation segment. Its code is unique within a tenant. */
data class DefaultViolationSegment(
    var code: String,
    var name: String,
    var title: String,
    var currency: String,
    var capacity: Int,
    var status: DefaultViolationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultViolationSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultViolationSegmentStatus): Boolean {
        when (status) {
            DefaultViolationSegmentStatus.NEW -> {
                when (next) {
                    DefaultViolationSegmentStatus.QUEUED, DefaultViolationSegmentStatus.FAILED -> return true
                }
                return false
            }
            DefaultViolationSegmentStatus.QUEUED -> return next == DefaultViolationSegmentStatus.DONE
            DefaultViolationSegmentStatus.RUNNING -> return next == DefaultViolationSegmentStatus.DONE || next == DefaultViolationSegmentStatus.FAILED
            DefaultViolationSegmentStatus.DONE -> return next == DefaultViolationSegmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultViolationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("default violation segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): DefaultViolationSegment {
        return DefaultViolationSegment(
            code = code,
            name = name,
            title = title,
            currency = currency,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default violation segment in status new. */
fun newDefaultViolationSegment(code: String, name: String): DefaultViolationSegment {
    return DefaultViolationSegment(
        code = code,
        name = name,
        title = "",
        currency = "",
        capacity = 0,
        status = DefaultViolationSegmentStatus.NEW,
    )
}
