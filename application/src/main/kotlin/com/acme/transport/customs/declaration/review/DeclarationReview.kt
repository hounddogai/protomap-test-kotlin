package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration review records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration review is in its life cycle. */
enum class DeclarationReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One declaration review, with what the planners need to act on it. */
data class DeclarationReview(
    var code: String,
    var title: String,
    var locale: String,
    var segment: String,
    var approved: Boolean,
    var maximum: Double,
    var label: String,
    var dock: String,
    var durationMinutes: Int,
    var phone: String,
    var status: DeclarationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationReviewStatus): Boolean {
        when (status) {
            DeclarationReviewStatus.REQUESTED -> {
                when (next) {
                    DeclarationReviewStatus.REVOKED -> return true
                }
                return false
            }
            DeclarationReviewStatus.GRANTED -> return next == DeclarationReviewStatus.EXPIRED
            DeclarationReviewStatus.REVOKED -> {
                when (next) {
                    DeclarationReviewStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationReviewStatus) {
        if (!canMoveTo(next)) {
            error("declaration review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun netExposure(): Double = round(durationMinutes.toDouble() * maximum * 100) / 100

    fun snapshot(): DeclarationReview {
        return DeclarationReview(
            code = code,
            title = title,
            locale = locale,
            segment = segment,
            approved = approved,
            maximum = maximum,
            label = label,
            dock = dock,
            durationMinutes = durationMinutes,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a declaration review in status requested. */
fun newDeclarationReview(code: String, title: String): DeclarationReview {
    return DeclarationReview(
        code = code,
        title = title,
        locale = "",
        segment = "",
        approved = false,
        maximum = 0.0,
        label = "",
        dock = "",
        durationMinutes = 0,
        phone = "",
        status = DeclarationReviewStatus.REQUESTED,
    )
}
