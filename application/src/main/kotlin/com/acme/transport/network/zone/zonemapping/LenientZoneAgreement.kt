package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient zone agreement is in its life cycle. */
enum class LenientZoneAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient zone agreement as the zone team keeps it. */
data class LenientZoneAgreement(
    var code: String,
    var label: String,
    var ratio: Double,
    var ownerId: String,
    var odometer: Int,
    var source: String,
    var stopCount: Int,
    var status: LenientZoneAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientZoneAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientZoneAgreementStatus): Boolean {
        when (status) {
            LenientZoneAgreementStatus.REQUESTED -> return next == LenientZoneAgreementStatus.EXPIRED
            LenientZoneAgreementStatus.GRANTED -> return next == LenientZoneAgreementStatus.REVOKED
            LenientZoneAgreementStatus.REVOKED -> {
                when (next) {
                    LenientZoneAgreementStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientZoneAgreementStatus) {
        if (!canMoveTo(next)) {
            error("lenient zone agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient zone agreement"} $code"

    fun weightedValue(): Double = round((ratio + stopCount.toDouble()) * 100) / 100

    fun snapshot(): LenientZoneAgreement {
        return LenientZoneAgreement(
            code = code,
            label = label,
            ratio = ratio,
            ownerId = ownerId,
            odometer = odometer,
            source = source,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient zone agreement in status requested. */
fun newLenientZoneAgreement(code: String, label: String): LenientZoneAgreement {
    return LenientZoneAgreement(
        code = code,
        label = label,
        ratio = 0.0,
        ownerId = "",
        odometer = 0,
        source = "",
        stopCount = 0,
        status = LenientZoneAgreementStatus.REQUESTED,
    )
}
