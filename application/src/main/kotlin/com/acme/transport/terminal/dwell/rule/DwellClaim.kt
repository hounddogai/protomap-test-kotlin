package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell claim is in its life cycle. */
enum class DwellClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One dwell claim, with what the planners need to act on it. */
data class DwellClaim(
    var code: String,
    var title: String,
    var minimum: Double,
    var limit: Int,
    var checksum: String,
    var contactEmail: String,
    var pinned: Boolean,
    var status: DwellClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellClaimStatus): Boolean {
        when (status) {
            DwellClaimStatus.DRAFT -> return next == DwellClaimStatus.SUSPENDED
            DwellClaimStatus.PENDING -> {
                when (next) {
                    DwellClaimStatus.ACTIVE, DwellClaimStatus.CLOSED -> return true
                }
                return false
            }
            DwellClaimStatus.ACTIVE -> return next == DwellClaimStatus.SUSPENDED || next == DwellClaimStatus.CLOSED
            DwellClaimStatus.SUSPENDED -> return next == DwellClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DwellClaimStatus) {
        if (!canMoveTo(next)) {
            error("dwell claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dwell claim"} $code"

    fun effectiveVolume(): Double = round((minimum + limit.toDouble()) * 100) / 100

    fun snapshot(): DwellClaim {
        return DwellClaim(
            code = code,
            title = title,
            minimum = minimum,
            limit = limit,
            checksum = checksum,
            contactEmail = contactEmail,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dwell claim in status draft. */
fun newDwellClaim(code: String, title: String): DwellClaim {
    return DwellClaim(
        code = code,
        title = title,
        minimum = 0.0,
        limit = 0,
        checksum = "",
        contactEmail = "",
        pinned = false,
        status = DwellClaimStatus.DRAFT,
    )
}
