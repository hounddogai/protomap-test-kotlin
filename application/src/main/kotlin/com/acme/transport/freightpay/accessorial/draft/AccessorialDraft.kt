package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The accessorial draft records of freightpay's accessorial work, and the rules, stores, and calculations
// around them.

/** Where a accessorial draft is in its life cycle. */
enum class AccessorialDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A accessorial draft. Its code is unique within a tenant. */
data class AccessorialDraft(
    var code: String,
    var name: String,
    var timezone: String,
    var loadKg: Double,
    var carrier: String,
    var checksum: String,
    var margin: Double,
    var ceiling: Double,
    var quantity: Int,
    var reviewer: String,
    var status: AccessorialDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialDraftStatus): Boolean {
        when (status) {
            AccessorialDraftStatus.NEW -> return next == AccessorialDraftStatus.DONE || next == AccessorialDraftStatus.FAILED
            AccessorialDraftStatus.QUEUED -> return next == AccessorialDraftStatus.RUNNING || next == AccessorialDraftStatus.DONE
            AccessorialDraftStatus.RUNNING -> return next == AccessorialDraftStatus.DONE || next == AccessorialDraftStatus.FAILED
            AccessorialDraftStatus.DONE -> {
                when (next) {
                    AccessorialDraftStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialDraftStatus) {
        if (!canMoveTo(next)) {
            error("accessorial draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round(quantity.toDouble() * margin * 100) / 100

    fun snapshot(): AccessorialDraft {
        return AccessorialDraft(
            code = code,
            name = name,
            timezone = timezone,
            loadKg = loadKg,
            carrier = carrier,
            checksum = checksum,
            margin = margin,
            ceiling = ceiling,
            quantity = quantity,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a accessorial draft in status new. */
fun newAccessorialDraft(code: String, name: String): AccessorialDraft {
    return AccessorialDraft(
        code = code,
        name = name,
        timezone = "",
        loadKg = 0.0,
        carrier = "",
        checksum = "",
        margin = 0.0,
        ceiling = 0.0,
        quantity = 0,
        reviewer = "",
        status = AccessorialDraftStatus.NEW,
    )
}
