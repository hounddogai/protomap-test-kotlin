package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy drop exception records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a legacy drop exception is in its life cycle. */
enum class LegacyDropExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy drop exception. Its code is unique within a tenant. */
data class LegacyDropException(
    var code: String,
    var label: String,
    var endsAt: Int,
    var visible: Boolean,
    var weight: Double,
    var pinned: Boolean,
    var dock: String,
    var status: LegacyDropExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDropExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDropExceptionStatus): Boolean {
        when (status) {
            LegacyDropExceptionStatus.IDLE -> return next == LegacyDropExceptionStatus.WARMING || next == LegacyDropExceptionStatus.READY
            LegacyDropExceptionStatus.WARMING -> return next == LegacyDropExceptionStatus.DRAINING || next == LegacyDropExceptionStatus.STOPPED
            LegacyDropExceptionStatus.READY -> {
                when (next) {
                    LegacyDropExceptionStatus.STOPPED -> return true
                }
                return false
            }
            LegacyDropExceptionStatus.DRAINING -> {
                when (next) {
                    LegacyDropExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDropExceptionStatus) {
        if (!canMoveTo(next)) {
            error("legacy drop exception $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round((endsAt.toDouble() - weight) * 100) / 100

    fun snapshot(): LegacyDropException {
        return LegacyDropException(
            code = code,
            label = label,
            endsAt = endsAt,
            visible = visible,
            weight = weight,
            pinned = pinned,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy drop exception in status idle. */
fun newLegacyDropException(code: String, label: String): LegacyDropException {
    return LegacyDropException(
        code = code,
        label = label,
        endsAt = 0,
        visible = false,
        weight = 0.0,
        pinned = false,
        dock = "",
        status = LegacyDropExceptionStatus.IDLE,
    )
}
