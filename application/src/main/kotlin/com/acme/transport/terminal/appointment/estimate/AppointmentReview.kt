package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment review is in its life cycle. */
enum class AppointmentReviewStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A appointment review. Its code is unique within a tenant. */
data class AppointmentReview(
    var code: String,
    var title: String,
    var revision: Int,
    var stopCount: Int,
    var name: String,
    var limit: Int,
    var summary: String,
    var timezone: String,
    var status: AppointmentReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentReviewStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentReviewStatus): Boolean {
        when (status) {
            AppointmentReviewStatus.DRAFT -> return next == AppointmentReviewStatus.ACTIVE || next == AppointmentReviewStatus.SUSPENDED
            AppointmentReviewStatus.PENDING -> {
                when (next) {
                    AppointmentReviewStatus.ACTIVE, AppointmentReviewStatus.CLOSED -> return true
                }
                return false
            }
            AppointmentReviewStatus.ACTIVE -> return next == AppointmentReviewStatus.SUSPENDED || next == AppointmentReviewStatus.CLOSED
            AppointmentReviewStatus.SUSPENDED -> {
                when (next) {
                    AppointmentReviewStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentReviewStatus) {
        if (!canMoveTo(next)) {
            error("appointment review $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun adjustedValue(): Double = round((stopCount.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): AppointmentReview {
        return AppointmentReview(
            code = code,
            title = title,
            revision = revision,
            stopCount = stopCount,
            name = name,
            limit = limit,
            summary = summary,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a appointment review in status draft. */
fun newAppointmentReview(code: String, title: String): AppointmentReview {
    return AppointmentReview(
        code = code,
        title = title,
        revision = 0,
        stopCount = 0,
        name = "",
        limit = 0,
        summary = "",
        timezone = "",
        status = AppointmentReviewStatus.DRAFT,
    )
}
