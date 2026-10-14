package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered locker agreement is in its life cycle. */
enum class TieredLockerAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered locker agreement as the locker team keeps it. */
data class TieredLockerAgreement(
    var code: String,
    var title: String,
    var odometer: Int,
    var fuelLiters: Double,
    var createdAt: Int,
    var threshold: Double,
    var region: String,
    var distanceKm: Double,
    var status: TieredLockerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredLockerAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredLockerAgreementStatus): Boolean {
        when (status) {
            TieredLockerAgreementStatus.IDLE -> return next == TieredLockerAgreementStatus.WARMING
            TieredLockerAgreementStatus.WARMING -> {
                when (next) {
                    TieredLockerAgreementStatus.READY, TieredLockerAgreementStatus.DRAINING -> return true
                }
                return false
            }
            TieredLockerAgreementStatus.READY -> {
                when (next) {
                    TieredLockerAgreementStatus.STOPPED -> return true
                }
                return false
            }
            TieredLockerAgreementStatus.DRAINING -> return next == TieredLockerAgreementStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredLockerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("tiered locker agreement $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered locker agreement"} $code"

    fun netTotal(): Double = round(distanceKm * odometer.toDouble() * 100) / 100

    fun snapshot(): TieredLockerAgreement {
        return TieredLockerAgreement(
            code = code,
            title = title,
            odometer = odometer,
            fuelLiters = fuelLiters,
            createdAt = createdAt,
            threshold = threshold,
            region = region,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tiered locker agreement in status idle. */
fun newTieredLockerAgreement(code: String, title: String): TieredLockerAgreement {
    return TieredLockerAgreement(
        code = code,
        title = title,
        odometer = 0,
        fuelLiters = 0.0,
        createdAt = 0,
        threshold = 0.0,
        region = "",
        distanceKm = 0.0,
        status = TieredLockerAgreementStatus.IDLE,
    )
}
