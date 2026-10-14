package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone agreement records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone agreement is in its life cycle. */
enum class ZoneAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A zone agreement. Its code is unique within a tenant. */
data class ZoneAgreement(
    var code: String,
    var title: String,
    var locale: String,
    var endsAt: Int,
    var floorValue: Double,
    var source: String,
    var status: ZoneAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneAgreementStatus): Boolean {
        when (status) {
            ZoneAgreementStatus.OPEN -> return next == ZoneAgreementStatus.ARCHIVED
            ZoneAgreementStatus.IN_REVIEW -> {
                when (next) {
                    ZoneAgreementStatus.RESOLVED, ZoneAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            ZoneAgreementStatus.RESOLVED -> {
                when (next) {
                    ZoneAgreementStatus.REOPENED, ZoneAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            ZoneAgreementStatus.REOPENED -> {
                when (next) {
                    ZoneAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneAgreementStatus) {
        if (!canMoveTo(next)) {
            error("zone agreement $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedVolume(): Double = round(endsAt.toDouble() * floorValue * 100) / 100

    fun snapshot(): ZoneAgreement {
        return ZoneAgreement(
            code = code,
            title = title,
            locale = locale,
            endsAt = endsAt,
            floorValue = floorValue,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a zone agreement in status open. */
fun newZoneAgreement(code: String, title: String): ZoneAgreement {
    return ZoneAgreement(
        code = code,
        title = title,
        locale = "",
        endsAt = 0,
        floorValue = 0.0,
        source = "",
        status = ZoneAgreementStatus.OPEN,
    )
}
