package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.text.normalize

/** Where a adaptive appointment review is in its life cycle. */
enum class AdaptiveAppointmentReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive appointment review. Its code is unique within a tenant. */
data class AdaptiveAppointmentReview(
    var code: String,
    var title: String,
    var visible: Boolean,
    var approved: Boolean,
    var expiresAt: Int,
    var archived: Boolean,
    var status: AdaptiveAppointmentReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAppointmentReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAppointmentReviewStatus): Boolean {
        when (status) {
            AdaptiveAppointmentReviewStatus.IDLE -> {
                when (next) {
                    AdaptiveAppointmentReviewStatus.READY, AdaptiveAppointmentReviewStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveAppointmentReviewStatus.WARMING -> {
                when (next) {
                    AdaptiveAppointmentReviewStatus.READY, AdaptiveAppointmentReviewStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveAppointmentReviewStatus.READY -> return next == AdaptiveAppointmentReviewStatus.DRAINING || next == AdaptiveAppointmentReviewStatus.STOPPED
            AdaptiveAppointmentReviewStatus.DRAINING -> {
                when (next) {
                    AdaptiveAppointmentReviewStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAppointmentReviewStatus) {
        if (!canMoveTo(next)) {
            error("adaptive appointment review $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): AdaptiveAppointmentReview {
        return AdaptiveAppointmentReview(
            code = code,
            title = title,
            visible = visible,
            approved = approved,
            expiresAt = expiresAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive appointment review in status idle. */
fun newAdaptiveAppointmentReview(code: String, title: String): AdaptiveAppointmentReview {
    return AdaptiveAppointmentReview(
        code = code,
        title = title,
        visible = false,
        approved = false,
        expiresAt = 0,
        archived = false,
        status = AdaptiveAppointmentReviewStatus.IDLE,
    )
}
