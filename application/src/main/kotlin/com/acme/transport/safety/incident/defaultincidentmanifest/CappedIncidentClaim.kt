package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped incident claim is in its life cycle. */
enum class CappedIncidentClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped incident claim as the incident team keeps it. */
data class CappedIncidentClaim(
    var code: String,
    var label: String,
    var description: String,
    var category: String,
    var locale: String,
    var notes: String,
    var quantity: Int,
    var routeCode: String,
    var currency: String,
    var pinned: Boolean,
    var expiresAt: Int,
    var status: CappedIncidentClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedIncidentClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedIncidentClaimStatus): Boolean {
        when (status) {
            CappedIncidentClaimStatus.NEW -> return next == CappedIncidentClaimStatus.RUNNING
            CappedIncidentClaimStatus.QUEUED -> return next == CappedIncidentClaimStatus.DONE || next == CappedIncidentClaimStatus.FAILED
            CappedIncidentClaimStatus.RUNNING -> return next == CappedIncidentClaimStatus.DONE
            CappedIncidentClaimStatus.DONE -> return next == CappedIncidentClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedIncidentClaimStatus) {
        if (!canMoveTo(next)) {
            error("capped incident claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedTotal(): Double = round((quantity.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): CappedIncidentClaim {
        return CappedIncidentClaim(
            code = code,
            label = label,
            description = description,
            category = category,
            locale = locale,
            notes = notes,
            quantity = quantity,
            routeCode = routeCode,
            currency = currency,
            pinned = pinned,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped incident claim in status new. */
fun newCappedIncidentClaim(code: String, label: String): CappedIncidentClaim {
    return CappedIncidentClaim(
        code = code,
        label = label,
        description = "",
        category = "",
        locale = "",
        notes = "",
        quantity = 0,
        routeCode = "",
        currency = "",
        pinned = false,
        expiresAt = 0,
        status = CappedIncidentClaimStatus.NEW,
    )
}
