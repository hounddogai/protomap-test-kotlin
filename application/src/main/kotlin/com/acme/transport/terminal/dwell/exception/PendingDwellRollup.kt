package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.text.normalize

/** Where a pending dwell rollup is in its life cycle. */
enum class PendingDwellRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending dwell rollup as the dwell team keeps it. */
data class PendingDwellRollup(
    var code: String,
    var title: String,
    var notes: String,
    var region: String,
    var shiftCode: String,
    var limit: Int,
    var ownerId: String,
    var status: PendingDwellRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDwellRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDwellRollupStatus): Boolean {
        when (status) {
            PendingDwellRollupStatus.CREATED -> return next == PendingDwellRollupStatus.PUBLISHED
            PendingDwellRollupStatus.VALIDATED -> {
                when (next) {
                    PendingDwellRollupStatus.RETIRED -> return true
                }
                return false
            }
            PendingDwellRollupStatus.PUBLISHED -> {
                when (next) {
                    PendingDwellRollupStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDwellRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending dwell rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending dwell rollup"} $code"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): PendingDwellRollup {
        return PendingDwellRollup(
            code = code,
            title = title,
            notes = notes,
            region = region,
            shiftCode = shiftCode,
            limit = limit,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending dwell rollup in status created. */
fun newPendingDwellRollup(code: String, title: String): PendingDwellRollup {
    return PendingDwellRollup(
        code = code,
        title = title,
        notes = "",
        region = "",
        shiftCode = "",
        limit = 0,
        ownerId = "",
        status = PendingDwellRollupStatus.CREATED,
    )
}
