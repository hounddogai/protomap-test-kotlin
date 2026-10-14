package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped trailer yard rollup is in its life cycle. */
enum class CappedTrailerYardRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped trailer yard rollup as the trailer yard team keeps it. */
data class CappedTrailerYardRollup(
    var code: String,
    var label: String,
    var pallets: Int,
    var country: String,
    var maximum: Double,
    var revision: Int,
    var contactEmail: String,
    var summary: String,
    var carrier: String,
    var endsAt: Int,
    var etaMinutes: Int,
    var status: CappedTrailerYardRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTrailerYardRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTrailerYardRollupStatus): Boolean {
        when (status) {
            CappedTrailerYardRollupStatus.DRAFT -> return next == CappedTrailerYardRollupStatus.ACTIVE || next == CappedTrailerYardRollupStatus.CLOSED
            CappedTrailerYardRollupStatus.PENDING -> return next == CappedTrailerYardRollupStatus.SUSPENDED || next == CappedTrailerYardRollupStatus.CLOSED
            CappedTrailerYardRollupStatus.ACTIVE -> {
                when (next) {
                    CappedTrailerYardRollupStatus.CLOSED -> return true
                }
                return false
            }
            CappedTrailerYardRollupStatus.SUSPENDED -> {
                when (next) {
                    CappedTrailerYardRollupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedTrailerYardRollupStatus) {
        if (!canMoveTo(next)) {
            error("capped trailer yard rollup $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"capped trailer yard rollup"} $code"

    fun netTotal(): Double = round(revision.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): CappedTrailerYardRollup {
        return CappedTrailerYardRollup(
            code = code,
            label = label,
            pallets = pallets,
            country = country,
            maximum = maximum,
            revision = revision,
            contactEmail = contactEmail,
            summary = summary,
            carrier = carrier,
            endsAt = endsAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped trailer yard rollup in status draft. */
fun newCappedTrailerYardRollup(code: String, label: String): CappedTrailerYardRollup {
    return CappedTrailerYardRollup(
        code = code,
        label = label,
        pallets = 0,
        country = "",
        maximum = 0.0,
        revision = 0,
        contactEmail = "",
        summary = "",
        carrier = "",
        endsAt = 0,
        etaMinutes = 0,
        status = CappedTrailerYardRollupStatus.DRAFT,
    )
}
