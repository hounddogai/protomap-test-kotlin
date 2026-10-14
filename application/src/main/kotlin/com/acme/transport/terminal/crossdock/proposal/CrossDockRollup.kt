package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.text.normalize

/** Where a cross dock rollup is in its life cycle. */
enum class CrossDockRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A cross dock rollup as the cross dock team keeps it. */
data class CrossDockRollup(
    var code: String,
    var label: String,
    var batchSize: Int,
    var description: String,
    var segment: String,
    var contactEmail: String,
    var status: CrossDockRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockRollupStatus): Boolean {
        when (status) {
            CrossDockRollupStatus.REQUESTED -> return next == CrossDockRollupStatus.GRANTED || next == CrossDockRollupStatus.REVOKED
            CrossDockRollupStatus.GRANTED -> return next == CrossDockRollupStatus.REVOKED
            CrossDockRollupStatus.REVOKED -> return next == CrossDockRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CrossDockRollupStatus) {
        if (!canMoveTo(next)) {
            error("cross dock rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): CrossDockRollup {
        return CrossDockRollup(
            code = code,
            label = label,
            batchSize = batchSize,
            description = description,
            segment = segment,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a cross dock rollup in status requested. */
fun newCrossDockRollup(code: String, label: String): CrossDockRollup {
    return CrossDockRollup(
        code = code,
        label = label,
        batchSize = 0,
        description = "",
        segment = "",
        contactEmail = "",
        status = CrossDockRollupStatus.REQUESTED,
    )
}
