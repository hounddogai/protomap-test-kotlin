package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient backhaul window is in its life cycle. */
enum class LenientBackhaulWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient backhaul window. Its code is unique within a tenant. */
data class LenientBackhaulWindow(
    var code: String,
    var title: String,
    var batchSize: Int,
    var sequence: Int,
    var segment: String,
    var active: Boolean,
    var status: LenientBackhaulWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientBackhaulWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientBackhaulWindowStatus): Boolean {
        when (status) {
            LenientBackhaulWindowStatus.REQUESTED -> {
                when (next) {
                    LenientBackhaulWindowStatus.REVOKED -> return true
                }
                return false
            }
            LenientBackhaulWindowStatus.GRANTED -> {
                when (next) {
                    LenientBackhaulWindowStatus.REVOKED -> return true
                }
                return false
            }
            LenientBackhaulWindowStatus.REVOKED -> return next == LenientBackhaulWindowStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientBackhaulWindowStatus) {
        if (!canMoveTo(next)) {
            error("lenient backhaul window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round((sequence.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): LenientBackhaulWindow {
        return LenientBackhaulWindow(
            code = code,
            title = title,
            batchSize = batchSize,
            sequence = sequence,
            segment = segment,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lenient backhaul window in status requested. */
fun newLenientBackhaulWindow(code: String, title: String): LenientBackhaulWindow {
    return LenientBackhaulWindow(
        code = code,
        title = title,
        batchSize = 0,
        sequence = 0,
        segment = "",
        active = false,
        status = LenientBackhaulWindowStatus.REQUESTED,
    )
}
