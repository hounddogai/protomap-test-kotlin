package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The drop digest records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop digest is in its life cycle. */
enum class DropDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A drop digest. Its code is unique within a tenant. */
data class DropDigest(
    var code: String,
    var label: String,
    var retries: Int,
    var weight: Double,
    var visible: Boolean,
    var status: DropDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropDigestStatus): Boolean {
        when (status) {
            DropDigestStatus.IDLE -> return next == DropDigestStatus.WARMING || next == DropDigestStatus.STOPPED
            DropDigestStatus.WARMING -> return next == DropDigestStatus.READY
            DropDigestStatus.READY -> {
                when (next) {
                    DropDigestStatus.STOPPED -> return true
                }
                return false
            }
            DropDigestStatus.DRAINING -> {
                when (next) {
                    DropDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropDigestStatus) {
        if (!canMoveTo(next)) {
            error("drop digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round((weight - retries.toDouble()) * 100) / 100

    fun snapshot(): DropDigest {
        return DropDigest(
            code = code,
            label = label,
            retries = retries,
            weight = weight,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a drop digest in status idle. */
fun newDropDigest(code: String, label: String): DropDigest {
    return DropDigest(
        code = code,
        label = label,
        retries = 0,
        weight = 0.0,
        visible = false,
        status = DropDigestStatus.IDLE,
    )
}
