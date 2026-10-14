package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial slot is in its life cycle. */
enum class AccessorialSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A accessorial slot. Its code is unique within a tenant. */
data class AccessorialSlot(
    var code: String,
    var title: String,
    var stopCount: Int,
    var createdAt: Int,
    var summary: String,
    var visible: Boolean,
    var tags: MutableList<String>,
    var amount: Double,
    var reviewer: String,
    var externalRef: String,
    var score: Double,
    var status: AccessorialSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialSlotStatus): Boolean {
        when (status) {
            AccessorialSlotStatus.REQUESTED -> return next == AccessorialSlotStatus.GRANTED
            AccessorialSlotStatus.GRANTED -> {
                when (next) {
                    AccessorialSlotStatus.REVOKED, AccessorialSlotStatus.EXPIRED -> return true
                }
                return false
            }
            AccessorialSlotStatus.REVOKED -> {
                when (next) {
                    AccessorialSlotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialSlotStatus) {
        if (!canMoveTo(next)) {
            error("accessorial slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round((stopCount.toDouble() - score) * 100) / 100

    fun snapshot(): AccessorialSlot {
        return AccessorialSlot(
            code = code,
            title = title,
            stopCount = stopCount,
            createdAt = createdAt,
            summary = summary,
            visible = visible,
            tags = tags,
            amount = amount,
            reviewer = reviewer,
            externalRef = externalRef,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a accessorial slot in status requested. */
fun newAccessorialSlot(code: String, title: String): AccessorialSlot {
    return AccessorialSlot(
        code = code,
        title = title,
        stopCount = 0,
        createdAt = 0,
        summary = "",
        visible = false,
        tags = mutableListOf<String>(),
        amount = 0.0,
        reviewer = "",
        externalRef = "",
        score = 0.0,
        status = AccessorialSlotStatus.REQUESTED,
    )
}
