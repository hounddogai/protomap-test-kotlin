package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation claim records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation claim is in its life cycle. */
enum class SortationClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A sortation claim as the sortation team keeps it. */
data class SortationClaim(
    var code: String,
    var label: String,
    var contactEmail: String,
    var shiftCode: String,
    var currency: String,
    var carrier: String,
    var revision: Int,
    var notes: String,
    var durationMinutes: Int,
    var graceDays: Int,
    var maximum: Double,
    var status: SortationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationClaimStatus): Boolean {
        when (status) {
            SortationClaimStatus.DRAFT -> {
                when (next) {
                    SortationClaimStatus.ACTIVE -> return true
                }
                return false
            }
            SortationClaimStatus.PENDING -> return next == SortationClaimStatus.ACTIVE || next == SortationClaimStatus.SUSPENDED
            SortationClaimStatus.ACTIVE -> {
                when (next) {
                    SortationClaimStatus.SUSPENDED, SortationClaimStatus.CLOSED -> return true
                }
                return false
            }
            SortationClaimStatus.SUSPENDED -> return next == SortationClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SortationClaimStatus) {
        if (!canMoveTo(next)) {
            error("sortation claim $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedVolume(): Double = round((durationMinutes.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): SortationClaim {
        return SortationClaim(
            code = code,
            label = label,
            contactEmail = contactEmail,
            shiftCode = shiftCode,
            currency = currency,
            carrier = carrier,
            revision = revision,
            notes = notes,
            durationMinutes = durationMinutes,
            graceDays = graceDays,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a sortation claim in status draft. */
fun newSortationClaim(code: String, label: String): SortationClaim {
    return SortationClaim(
        code = code,
        label = label,
        contactEmail = "",
        shiftCode = "",
        currency = "",
        carrier = "",
        revision = 0,
        notes = "",
        durationMinutes = 0,
        graceDays = 0,
        maximum = 0.0,
        status = SortationClaimStatus.DRAFT,
    )
}
