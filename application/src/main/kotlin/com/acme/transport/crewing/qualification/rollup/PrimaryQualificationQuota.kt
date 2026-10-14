package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary qualification quota is in its life cycle. */
enum class PrimaryQualificationQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary qualification quota. Its code is unique within a tenant. */
data class PrimaryQualificationQuota(
    var code: String,
    var title: String,
    var currency: String,
    var country: String,
    var region: String,
    var ownerId: String,
    var retries: Int,
    var sequence: Int,
    var updatedAt: Int,
    var status: PrimaryQualificationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryQualificationQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryQualificationQuotaStatus): Boolean {
        when (status) {
            PrimaryQualificationQuotaStatus.OPEN -> return next == PrimaryQualificationQuotaStatus.IN_REVIEW
            PrimaryQualificationQuotaStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryQualificationQuotaStatus.RESOLVED, PrimaryQualificationQuotaStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryQualificationQuotaStatus.RESOLVED -> {
                when (next) {
                    PrimaryQualificationQuotaStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryQualificationQuotaStatus.REOPENED -> {
                when (next) {
                    PrimaryQualificationQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryQualificationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("primary qualification quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedTotal(): Double = round(sequence.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): PrimaryQualificationQuota {
        return PrimaryQualificationQuota(
            code = code,
            title = title,
            currency = currency,
            country = country,
            region = region,
            ownerId = ownerId,
            retries = retries,
            sequence = sequence,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary qualification quota in status open. */
fun newPrimaryQualificationQuota(code: String, title: String): PrimaryQualificationQuota {
    return PrimaryQualificationQuota(
        code = code,
        title = title,
        currency = "",
        country = "",
        region = "",
        ownerId = "",
        retries = 0,
        sequence = 0,
        updatedAt = 0,
        status = PrimaryQualificationQuotaStatus.OPEN,
    )
}
