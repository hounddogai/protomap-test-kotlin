package com.acme.transport.crewing.qualification.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default qualification agreement is in its life cycle. */
enum class DefaultQualificationAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default qualification agreement. Its code is unique within a tenant. */
data class DefaultQualificationAgreement(
    var code: String,
    var title: String,
    var checksum: String,
    var category: String,
    var minimum: Double,
    var distanceKm: Double,
    var amount: Double,
    var externalRef: String,
    var ownerId: String,
    var status: DefaultQualificationAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultQualificationAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultQualificationAgreementStatus): Boolean {
        when (status) {
            DefaultQualificationAgreementStatus.OPEN -> {
                when (next) {
                    DefaultQualificationAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultQualificationAgreementStatus.IN_REVIEW -> {
                when (next) {
                    DefaultQualificationAgreementStatus.REOPENED, DefaultQualificationAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultQualificationAgreementStatus.RESOLVED -> {
                when (next) {
                    DefaultQualificationAgreementStatus.REOPENED, DefaultQualificationAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultQualificationAgreementStatus.REOPENED -> return next == DefaultQualificationAgreementStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultQualificationAgreementStatus) {
        if (!canMoveTo(next)) {
            error("default qualification agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default qualification agreement"} $code"

    fun projectedVolume(): Double = round(amount * minimum * 100) / 100

    fun snapshot(): DefaultQualificationAgreement {
        return DefaultQualificationAgreement(
            code = code,
            title = title,
            checksum = checksum,
            category = category,
            minimum = minimum,
            distanceKm = distanceKm,
            amount = amount,
            externalRef = externalRef,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default qualification agreement in status open. */
fun newDefaultQualificationAgreement(code: String, title: String): DefaultQualificationAgreement {
    return DefaultQualificationAgreement(
        code = code,
        title = title,
        checksum = "",
        category = "",
        minimum = 0.0,
        distanceKm = 0.0,
        amount = 0.0,
        externalRef = "",
        ownerId = "",
        status = DefaultQualificationAgreementStatus.OPEN,
    )
}
