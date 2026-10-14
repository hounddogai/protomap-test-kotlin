package com.acme.transport.fleetops.tire.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire request is in its life cycle. */
enum class TireRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tire request as the tire team keeps it. */
data class TireRequest(
    var code: String,
    var title: String,
    var externalRef: String,
    var amount: Double,
    var ownerId: String,
    var margin: Double,
    var etaMinutes: Int,
    var archived: Boolean,
    var ratio: Double,
    var status: TireRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireRequestStatus): Boolean {
        when (status) {
            TireRequestStatus.REQUESTED -> {
                when (next) {
                    TireRequestStatus.REVOKED, TireRequestStatus.EXPIRED -> return true
                }
                return false
            }
            TireRequestStatus.GRANTED -> {
                when (next) {
                    TireRequestStatus.REVOKED, TireRequestStatus.EXPIRED -> return true
                }
                return false
            }
            TireRequestStatus.REVOKED -> {
                when (next) {
                    TireRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireRequestStatus) {
        if (!canMoveTo(next)) {
            error("tire request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round((margin + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TireRequest {
        return TireRequest(
            code = code,
            title = title,
            externalRef = externalRef,
            amount = amount,
            ownerId = ownerId,
            margin = margin,
            etaMinutes = etaMinutes,
            archived = archived,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tire request in status requested. */
fun newTireRequest(code: String, title: String): TireRequest {
    return TireRequest(
        code = code,
        title = title,
        externalRef = "",
        amount = 0.0,
        ownerId = "",
        margin = 0.0,
        etaMinutes = 0,
        archived = false,
        ratio = 0.0,
        status = TireRequestStatus.REQUESTED,
    )
}
