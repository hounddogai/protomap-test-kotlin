package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending parcel request is in its life cycle. */
enum class PendingParcelRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending parcel request as the parcel team keeps it. */
data class PendingParcelRequest(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var distanceKm: Double,
    var budget: Double,
    var retries: Int,
    var attempts: Int,
    var quantity: Int,
    var active: Boolean,
    var status: PendingParcelRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingParcelRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingParcelRequestStatus): Boolean {
        when (status) {
            PendingParcelRequestStatus.OPEN -> {
                when (next) {
                    PendingParcelRequestStatus.IN_REVIEW, PendingParcelRequestStatus.RESOLVED -> return true
                }
                return false
            }
            PendingParcelRequestStatus.IN_REVIEW -> {
                when (next) {
                    PendingParcelRequestStatus.REOPENED, PendingParcelRequestStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingParcelRequestStatus.RESOLVED -> return next == PendingParcelRequestStatus.ARCHIVED
            PendingParcelRequestStatus.REOPENED -> {
                when (next) {
                    PendingParcelRequestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingParcelRequestStatus) {
        if (!canMoveTo(next)) {
            error("pending parcel request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round(budget * attempts.toDouble() * 100) / 100

    fun snapshot(): PendingParcelRequest {
        return PendingParcelRequest(
            code = code,
            name = name,
            tags = tags,
            distanceKm = distanceKm,
            budget = budget,
            retries = retries,
            attempts = attempts,
            quantity = quantity,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending parcel request in status open. */
fun newPendingParcelRequest(code: String, name: String): PendingParcelRequest {
    return PendingParcelRequest(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        distanceKm = 0.0,
        budget = 0.0,
        retries = 0,
        attempts = 0,
        quantity = 0,
        active = false,
        status = PendingParcelRequestStatus.OPEN,
    )
}
