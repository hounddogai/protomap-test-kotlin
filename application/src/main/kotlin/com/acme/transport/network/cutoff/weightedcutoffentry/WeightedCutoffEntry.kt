package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted cutoff entry records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a weighted cutoff entry is in its life cycle. */
enum class WeightedCutoffEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted cutoff entry. Its code is unique within a tenant. */
data class WeightedCutoffEntry(
    var code: String,
    var label: String,
    var loadKg: Double,
    var ownerId: String,
    var etaMinutes: Int,
    var attempts: Int,
    var contactEmail: String,
    var status: WeightedCutoffEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCutoffEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCutoffEntryStatus): Boolean {
        when (status) {
            WeightedCutoffEntryStatus.PROPOSED -> return next == WeightedCutoffEntryStatus.APPROVED || next == WeightedCutoffEntryStatus.REJECTED
            WeightedCutoffEntryStatus.APPROVED -> return next == WeightedCutoffEntryStatus.REJECTED || next == WeightedCutoffEntryStatus.WITHDRAWN
            WeightedCutoffEntryStatus.REJECTED -> {
                when (next) {
                    WeightedCutoffEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCutoffEntryStatus) {
        if (!canMoveTo(next)) {
            error("weighted cutoff entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedLoad(): Double = round((attempts.toDouble() - loadKg) * 100) / 100

    fun snapshot(): WeightedCutoffEntry {
        return WeightedCutoffEntry(
            code = code,
            label = label,
            loadKg = loadKg,
            ownerId = ownerId,
            etaMinutes = etaMinutes,
            attempts = attempts,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted cutoff entry in status proposed. */
fun newWeightedCutoffEntry(code: String, label: String): WeightedCutoffEntry {
    return WeightedCutoffEntry(
        code = code,
        label = label,
        loadKg = 0.0,
        ownerId = "",
        etaMinutes = 0,
        attempts = 0,
        contactEmail = "",
        status = WeightedCutoffEntryStatus.PROPOSED,
    )
}
