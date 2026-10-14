package com.acme.transport.freightpay.accessorial.lenientaccessorialentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual accessorial entry records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a manual accessorial entry is in its life cycle. */
enum class ManualAccessorialEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual accessorial entry as the accessorial team keeps it. */
data class ManualAccessorialEntry(
    var code: String,
    var label: String,
    var revision: Int,
    var rate: Double,
    var archived: Boolean,
    var etaMinutes: Int,
    var status: ManualAccessorialEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAccessorialEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAccessorialEntryStatus): Boolean {
        when (status) {
            ManualAccessorialEntryStatus.PROPOSED -> return next == ManualAccessorialEntryStatus.APPROVED
            ManualAccessorialEntryStatus.APPROVED -> return next == ManualAccessorialEntryStatus.REJECTED
            ManualAccessorialEntryStatus.REJECTED -> {
                when (next) {
                    ManualAccessorialEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAccessorialEntryStatus) {
        if (!canMoveTo(next)) {
            error("manual accessorial entry $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round(etaMinutes.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): ManualAccessorialEntry {
        return ManualAccessorialEntry(
            code = code,
            label = label,
            revision = revision,
            rate = rate,
            archived = archived,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual accessorial entry in status proposed. */
fun newManualAccessorialEntry(code: String, label: String): ManualAccessorialEntry {
    return ManualAccessorialEntry(
        code = code,
        label = label,
        revision = 0,
        rate = 0.0,
        archived = false,
        etaMinutes = 0,
        status = ManualAccessorialEntryStatus.PROPOSED,
    )
}
