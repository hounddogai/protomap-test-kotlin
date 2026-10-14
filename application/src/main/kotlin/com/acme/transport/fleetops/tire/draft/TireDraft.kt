package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tire draft records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire draft is in its life cycle. */
enum class TireDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tire draft and its current status. */
data class TireDraft(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var unit: String,
    var limit: Int,
    var rate: Double,
    var depot: String,
    var createdAt: Int,
    var checksum: String,
    var status: TireDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireDraftStatus): Boolean {
        when (status) {
            TireDraftStatus.REQUESTED -> {
                when (next) {
                    TireDraftStatus.EXPIRED -> return true
                }
                return false
            }
            TireDraftStatus.GRANTED -> return next == TireDraftStatus.REVOKED
            TireDraftStatus.REVOKED -> return next == TireDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TireDraftStatus) {
        if (!canMoveTo(next)) {
            error("tire draft $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedTotal(): Double = round(createdAt.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): TireDraft {
        return TireDraft(
            code = code,
            label = label,
            expiresAt = expiresAt,
            unit = unit,
            limit = limit,
            rate = rate,
            depot = depot,
            createdAt = createdAt,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tire draft in status requested. */
fun newTireDraft(code: String, label: String): TireDraft {
    return TireDraft(
        code = code,
        label = label,
        expiresAt = 0,
        unit = "",
        limit = 0,
        rate = 0.0,
        depot = "",
        createdAt = 0,
        checksum = "",
        status = TireDraftStatus.REQUESTED,
    )
}
