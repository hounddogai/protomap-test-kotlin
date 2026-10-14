package com.acme.transport.customs.clearance.preference

import com.acme.transport.kit.text.normalize

/** Where a clearance rollup is in its life cycle. */
enum class ClearanceRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a clearance rollup and its current status. */
data class ClearanceRollup(
    var code: String,
    var name: String,
    var active: Boolean,
    var description: String,
    var contactEmail: String,
    var country: String,
    var score: Double,
    var status: ClearanceRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceRollupStatus): Boolean {
        when (status) {
            ClearanceRollupStatus.NEW -> {
                when (next) {
                    ClearanceRollupStatus.QUEUED, ClearanceRollupStatus.DONE -> return true
                }
                return false
            }
            ClearanceRollupStatus.QUEUED -> return next == ClearanceRollupStatus.RUNNING || next == ClearanceRollupStatus.DONE
            ClearanceRollupStatus.RUNNING -> return next == ClearanceRollupStatus.DONE || next == ClearanceRollupStatus.FAILED
            ClearanceRollupStatus.DONE -> {
                when (next) {
                    ClearanceRollupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceRollupStatus) {
        if (!canMoveTo(next)) {
            error("clearance rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"clearance rollup"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): ClearanceRollup {
        return ClearanceRollup(
            code = code,
            name = name,
            active = active,
            description = description,
            contactEmail = contactEmail,
            country = country,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a clearance rollup in status new. */
fun newClearanceRollup(code: String, name: String): ClearanceRollup {
    return ClearanceRollup(
        code = code,
        name = name,
        active = false,
        description = "",
        contactEmail = "",
        country = "",
        score = 0.0,
        status = ClearanceRollupStatus.NEW,
    )
}
