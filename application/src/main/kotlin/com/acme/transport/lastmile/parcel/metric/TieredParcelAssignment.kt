package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.text.normalize

/** Where a tiered parcel assignment is in its life cycle. */
enum class TieredParcelAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tiered parcel assignment and its current status. */
data class TieredParcelAssignment(
    var code: String,
    var name: String,
    var active: Boolean,
    var currency: String,
    var retries: Int,
    var status: TieredParcelAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredParcelAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredParcelAssignmentStatus): Boolean {
        when (status) {
            TieredParcelAssignmentStatus.NEW -> return next == TieredParcelAssignmentStatus.FAILED
            TieredParcelAssignmentStatus.QUEUED -> {
                when (next) {
                    TieredParcelAssignmentStatus.RUNNING, TieredParcelAssignmentStatus.FAILED -> return true
                }
                return false
            }
            TieredParcelAssignmentStatus.RUNNING -> {
                when (next) {
                    TieredParcelAssignmentStatus.DONE, TieredParcelAssignmentStatus.FAILED -> return true
                }
                return false
            }
            TieredParcelAssignmentStatus.DONE -> {
                when (next) {
                    TieredParcelAssignmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredParcelAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered parcel assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): TieredParcelAssignment {
        return TieredParcelAssignment(
            code = code,
            name = name,
            active = active,
            currency = currency,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered parcel assignment in status new. */
fun newTieredParcelAssignment(code: String, name: String): TieredParcelAssignment {
    return TieredParcelAssignment(
        code = code,
        name = name,
        active = false,
        currency = "",
        retries = 0,
        status = TieredParcelAssignmentStatus.NEW,
    )
}
