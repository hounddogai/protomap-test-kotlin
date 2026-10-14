package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped redelivery window is in its life cycle. */
enum class CappedRedeliveryWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped redelivery window. Its code is unique within a tenant. */
data class CappedRedeliveryWindow(
    var code: String,
    var label: String,
    var checksum: String,
    var contactEmail: String,
    var stopCount: Int,
    var visible: Boolean,
    var notes: String,
    var ownerId: String,
    var dock: String,
    var threshold: Double,
    var description: String,
    var status: CappedRedeliveryWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRedeliveryWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRedeliveryWindowStatus): Boolean {
        when (status) {
            CappedRedeliveryWindowStatus.OPEN -> {
                when (next) {
                    CappedRedeliveryWindowStatus.IN_REVIEW -> return true
                }
                return false
            }
            CappedRedeliveryWindowStatus.IN_REVIEW -> {
                when (next) {
                    CappedRedeliveryWindowStatus.REOPENED, CappedRedeliveryWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedRedeliveryWindowStatus.RESOLVED -> {
                when (next) {
                    CappedRedeliveryWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedRedeliveryWindowStatus.REOPENED -> return next == CappedRedeliveryWindowStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedRedeliveryWindowStatus) {
        if (!canMoveTo(next)) {
            error("capped redelivery window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capped redelivery window"} $code"

    fun weightedLoad(): Double = round((stopCount.toDouble() - threshold) * 100) / 100

    fun snapshot(): CappedRedeliveryWindow {
        return CappedRedeliveryWindow(
            code = code,
            label = label,
            checksum = checksum,
            contactEmail = contactEmail,
            stopCount = stopCount,
            visible = visible,
            notes = notes,
            ownerId = ownerId,
            dock = dock,
            threshold = threshold,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped redelivery window in status open. */
fun newCappedRedeliveryWindow(code: String, label: String): CappedRedeliveryWindow {
    return CappedRedeliveryWindow(
        code = code,
        label = label,
        checksum = "",
        contactEmail = "",
        stopCount = 0,
        visible = false,
        notes = "",
        ownerId = "",
        dock = "",
        threshold = 0.0,
        description = "",
        status = CappedRedeliveryWindowStatus.OPEN,
    )
}
