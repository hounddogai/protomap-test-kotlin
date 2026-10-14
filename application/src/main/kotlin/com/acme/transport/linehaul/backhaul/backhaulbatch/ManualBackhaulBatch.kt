package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.text.normalize

// The manual backhaul batch records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a manual backhaul batch is in its life cycle. */
enum class ManualBackhaulBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual backhaul batch. Its code is unique within a tenant. */
data class ManualBackhaulBatch(
    var code: String,
    var label: String,
    var externalRef: String,
    var endsAt: Int,
    var lane: String,
    var contactEmail: String,
    var source: String,
    var status: ManualBackhaulBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBackhaulBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBackhaulBatchStatus): Boolean {
        when (status) {
            ManualBackhaulBatchStatus.PLANNED -> return next == ManualBackhaulBatchStatus.COMPLETED || next == ManualBackhaulBatchStatus.CANCELLED
            ManualBackhaulBatchStatus.SCHEDULED -> return next == ManualBackhaulBatchStatus.IN_PROGRESS
            ManualBackhaulBatchStatus.IN_PROGRESS -> {
                when (next) {
                    ManualBackhaulBatchStatus.CANCELLED -> return true
                }
                return false
            }
            ManualBackhaulBatchStatus.COMPLETED -> {
                when (next) {
                    ManualBackhaulBatchStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualBackhaulBatchStatus) {
        if (!canMoveTo(next)) {
            error("manual backhaul batch $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): ManualBackhaulBatch {
        return ManualBackhaulBatch(
            code = code,
            label = label,
            externalRef = externalRef,
            endsAt = endsAt,
            lane = lane,
            contactEmail = contactEmail,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual backhaul batch in status planned. */
fun newManualBackhaulBatch(code: String, label: String): ManualBackhaulBatch {
    return ManualBackhaulBatch(
        code = code,
        label = label,
        externalRef = "",
        endsAt = 0,
        lane = "",
        contactEmail = "",
        source = "",
        status = ManualBackhaulBatchStatus.PLANNED,
    )
}
