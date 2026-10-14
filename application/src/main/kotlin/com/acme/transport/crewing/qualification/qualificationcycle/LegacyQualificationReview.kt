package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy qualification review is in its life cycle. */
enum class LegacyQualificationReviewStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy qualification review as the qualification team keeps it. */
data class LegacyQualificationReview(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var channel: String,
    var phone: String,
    var checksum: String,
    var currency: String,
    var active: Boolean,
    var fuelLiters: Double,
    var segment: String,
    var status: LegacyQualificationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyQualificationReviewStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyQualificationReviewStatus): Boolean {
        when (status) {
            LegacyQualificationReviewStatus.DRAFT -> return next == LegacyQualificationReviewStatus.ACTIVE
            LegacyQualificationReviewStatus.PENDING -> {
                when (next) {
                    LegacyQualificationReviewStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyQualificationReviewStatus.ACTIVE -> {
                when (next) {
                    LegacyQualificationReviewStatus.SUSPENDED, LegacyQualificationReviewStatus.CLOSED -> return true
                }
                return false
            }
            LegacyQualificationReviewStatus.SUSPENDED -> {
                when (next) {
                    LegacyQualificationReviewStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyQualificationReviewStatus) {
        if (!canMoveTo(next)) {
            error("legacy qualification review $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round((fuelLiters + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyQualificationReview {
        return LegacyQualificationReview(
            code = code,
            title = title,
            updatedAt = updatedAt,
            channel = channel,
            phone = phone,
            checksum = checksum,
            currency = currency,
            active = active,
            fuelLiters = fuelLiters,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy qualification review in status draft. */
fun newLegacyQualificationReview(code: String, title: String): LegacyQualificationReview {
    return LegacyQualificationReview(
        code = code,
        title = title,
        updatedAt = 0,
        channel = "",
        phone = "",
        checksum = "",
        currency = "",
        active = false,
        fuelLiters = 0.0,
        segment = "",
        status = LegacyQualificationReviewStatus.DRAFT,
    )
}
