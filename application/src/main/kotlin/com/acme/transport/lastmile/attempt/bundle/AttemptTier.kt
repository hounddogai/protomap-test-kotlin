package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt tier is in its life cycle. */
enum class AttemptTierStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A attempt tier. Its code is unique within a tenant. */
data class AttemptTier(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var approved: Boolean,
    var locale: String,
    var pinned: Boolean,
    var reviewer: String,
    var batchSize: Int,
    var endsAt: Int,
    var status: AttemptTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptTierStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptTierStatus): Boolean {
        when (status) {
            AttemptTierStatus.DRAFT -> {
                when (next) {
                    AttemptTierStatus.CLOSED -> return true
                }
                return false
            }
            AttemptTierStatus.PENDING -> {
                when (next) {
                    AttemptTierStatus.SUSPENDED -> return true
                }
                return false
            }
            AttemptTierStatus.ACTIVE -> {
                when (next) {
                    AttemptTierStatus.SUSPENDED -> return true
                }
                return false
            }
            AttemptTierStatus.SUSPENDED -> {
                when (next) {
                    AttemptTierStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptTierStatus) {
        if (!canMoveTo(next)) {
            error("attempt tier $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round((batchSize.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): AttemptTier {
        return AttemptTier(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            approved = approved,
            locale = locale,
            pinned = pinned,
            reviewer = reviewer,
            batchSize = batchSize,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a attempt tier in status draft. */
fun newAttemptTier(code: String, label: String): AttemptTier {
    return AttemptTier(
        code = code,
        label = label,
        etaMinutes = 0,
        approved = false,
        locale = "",
        pinned = false,
        reviewer = "",
        batchSize = 0,
        endsAt = 0,
        status = AttemptTierStatus.DRAFT,
    )
}
