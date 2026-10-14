package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.text.normalize

/** Where a attempt target is in its life cycle. */
enum class AttemptTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A attempt target as the attempt team keeps it. */
data class AttemptTarget(
    var code: String,
    var title: String,
    var trailerId: String,
    var summary: String,
    var depot: String,
    var checksum: String,
    var label: String,
    var quantity: Int,
    var status: AttemptTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptTargetStatus): Boolean {
        when (status) {
            AttemptTargetStatus.CREATED -> {
                when (next) {
                    AttemptTargetStatus.VALIDATED, AttemptTargetStatus.RETIRED -> return true
                }
                return false
            }
            AttemptTargetStatus.VALIDATED -> return next == AttemptTargetStatus.PUBLISHED
            AttemptTargetStatus.PUBLISHED -> {
                when (next) {
                    AttemptTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptTargetStatus) {
        if (!canMoveTo(next)) {
            error("attempt target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): AttemptTarget {
        return AttemptTarget(
            code = code,
            title = title,
            trailerId = trailerId,
            summary = summary,
            depot = depot,
            checksum = checksum,
            label = label,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a attempt target in status created. */
fun newAttemptTarget(code: String, title: String): AttemptTarget {
    return AttemptTarget(
        code = code,
        title = title,
        trailerId = "",
        summary = "",
        depot = "",
        checksum = "",
        label = "",
        quantity = 0,
        status = AttemptTargetStatus.CREATED,
    )
}
