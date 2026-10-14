package com.acme.transport.crewing.swap.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling swap agreement records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a rolling swap agreement is in its life cycle. */
enum class RollingSwapAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rolling swap agreement, with what the planners need to act on it. */
data class RollingSwapAgreement(
    var code: String,
    var title: String,
    var name: String,
    var expiresAt: Int,
    var version: Int,
    var distanceKm: Double,
    var limit: Int,
    var endsAt: Int,
    var floorValue: Double,
    var attempts: Int,
    var approved: Boolean,
    var status: RollingSwapAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSwapAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSwapAgreementStatus): Boolean {
        when (status) {
            RollingSwapAgreementStatus.NEW -> return next == RollingSwapAgreementStatus.RUNNING
            RollingSwapAgreementStatus.QUEUED -> {
                when (next) {
                    RollingSwapAgreementStatus.FAILED -> return true
                }
                return false
            }
            RollingSwapAgreementStatus.RUNNING -> {
                when (next) {
                    RollingSwapAgreementStatus.DONE, RollingSwapAgreementStatus.FAILED -> return true
                }
                return false
            }
            RollingSwapAgreementStatus.DONE -> return next == RollingSwapAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingSwapAgreementStatus) {
        if (!canMoveTo(next)) {
            error("rolling swap agreement $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"rolling swap agreement"} $code"

    fun adjustedTotal(): Double = round((limit.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): RollingSwapAgreement {
        return RollingSwapAgreement(
            code = code,
            title = title,
            name = name,
            expiresAt = expiresAt,
            version = version,
            distanceKm = distanceKm,
            limit = limit,
            endsAt = endsAt,
            floorValue = floorValue,
            attempts = attempts,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling swap agreement in status new. */
fun newRollingSwapAgreement(code: String, title: String): RollingSwapAgreement {
    return RollingSwapAgreement(
        code = code,
        title = title,
        name = "",
        expiresAt = 0,
        version = 0,
        distanceKm = 0.0,
        limit = 0,
        endsAt = 0,
        floorValue = 0.0,
        attempts = 0,
        approved = false,
        status = RollingSwapAgreementStatus.NEW,
    )
}
