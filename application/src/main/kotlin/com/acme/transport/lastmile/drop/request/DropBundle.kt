package com.acme.transport.lastmile.drop.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop bundle is in its life cycle. */
enum class DropBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a drop bundle and its current status. */
data class DropBundle(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var sequence: Int,
    var description: String,
    var discount: Double,
    var revision: Int,
    var externalRef: String,
    var status: DropBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropBundleStatus): Boolean {
        when (status) {
            DropBundleStatus.OPEN -> return next == DropBundleStatus.IN_REVIEW
            DropBundleStatus.IN_REVIEW -> return next == DropBundleStatus.RESOLVED
            DropBundleStatus.RESOLVED -> return next == DropBundleStatus.ARCHIVED
            DropBundleStatus.REOPENED -> {
                when (next) {
                    DropBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropBundleStatus) {
        if (!canMoveTo(next)) {
            error("drop bundle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"drop bundle"} $code"

    fun projectedTotal(): Double = round((discount - revision.toDouble()) * 100) / 100

    fun snapshot(): DropBundle {
        return DropBundle(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            sequence = sequence,
            description = description,
            discount = discount,
            revision = revision,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a drop bundle in status open. */
fun newDropBundle(code: String, label: String): DropBundle {
    return DropBundle(
        code = code,
        label = label,
        durationMinutes = 0,
        sequence = 0,
        description = "",
        discount = 0.0,
        revision = 0,
        externalRef = "",
        status = DropBundleStatus.OPEN,
    )
}
