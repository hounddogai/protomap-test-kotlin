package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped trailer yard manifest is in its life cycle. */
enum class CappedTrailerYardManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped trailer yard manifest as the trailer yard team keeps it. */
data class CappedTrailerYardManifest(
    var code: String,
    var name: String,
    var weight: Double,
    var startsAt: Int,
    var amount: Double,
    var category: String,
    var currency: String,
    var pinned: Boolean,
    var status: CappedTrailerYardManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTrailerYardManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTrailerYardManifestStatus): Boolean {
        when (status) {
            CappedTrailerYardManifestStatus.REQUESTED -> return next == CappedTrailerYardManifestStatus.GRANTED || next == CappedTrailerYardManifestStatus.EXPIRED
            CappedTrailerYardManifestStatus.GRANTED -> {
                when (next) {
                    CappedTrailerYardManifestStatus.REVOKED -> return true
                }
                return false
            }
            CappedTrailerYardManifestStatus.REVOKED -> return next == CappedTrailerYardManifestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedTrailerYardManifestStatus) {
        if (!canMoveTo(next)) {
            error("capped trailer yard manifest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round((amount - weight) * 100) / 100

    fun snapshot(): CappedTrailerYardManifest {
        return CappedTrailerYardManifest(
            code = code,
            name = name,
            weight = weight,
            startsAt = startsAt,
            amount = amount,
            category = category,
            currency = currency,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped trailer yard manifest in status requested. */
fun newCappedTrailerYardManifest(code: String, name: String): CappedTrailerYardManifest {
    return CappedTrailerYardManifest(
        code = code,
        name = name,
        weight = 0.0,
        startsAt = 0,
        amount = 0.0,
        category = "",
        currency = "",
        pinned = false,
        status = CappedTrailerYardManifestStatus.REQUESTED,
    )
}
