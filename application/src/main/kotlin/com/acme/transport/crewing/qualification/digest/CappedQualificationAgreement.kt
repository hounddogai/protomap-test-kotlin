package com.acme.transport.crewing.qualification.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped qualification agreement is in its life cycle. */
enum class CappedQualificationAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped qualification agreement as the qualification team keeps it. */
data class CappedQualificationAgreement(
    var code: String,
    var name: String,
    var version: Int,
    var revision: Int,
    var title: String,
    var threshold: Double,
    var vehicleId: String,
    var endsAt: Int,
    var status: CappedQualificationAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedQualificationAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedQualificationAgreementStatus): Boolean {
        when (status) {
            CappedQualificationAgreementStatus.REQUESTED -> {
                when (next) {
                    CappedQualificationAgreementStatus.GRANTED, CappedQualificationAgreementStatus.REVOKED -> return true
                }
                return false
            }
            CappedQualificationAgreementStatus.GRANTED -> return next == CappedQualificationAgreementStatus.REVOKED || next == CappedQualificationAgreementStatus.EXPIRED
            CappedQualificationAgreementStatus.REVOKED -> return next == CappedQualificationAgreementStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedQualificationAgreementStatus) {
        if (!canMoveTo(next)) {
            error("capped qualification agreement $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((threshold + version.toDouble()) * 100) / 100

    fun snapshot(): CappedQualificationAgreement {
        return CappedQualificationAgreement(
            code = code,
            name = name,
            version = version,
            revision = revision,
            title = title,
            threshold = threshold,
            vehicleId = vehicleId,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped qualification agreement in status requested. */
fun newCappedQualificationAgreement(code: String, name: String): CappedQualificationAgreement {
    return CappedQualificationAgreement(
        code = code,
        name = name,
        version = 0,
        revision = 0,
        title = "",
        threshold = 0.0,
        vehicleId = "",
        endsAt = 0,
        status = CappedQualificationAgreementStatus.REQUESTED,
    )
}
