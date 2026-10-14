package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled locker review is in its life cycle. */
enum class ScheduledLockerReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled locker review. Its code is unique within a tenant. */
data class ScheduledLockerReview(
    var code: String,
    var name: String,
    var description: String,
    var amount: Double,
    var phone: String,
    var checksum: String,
    var sequence: Int,
    var driverId: String,
    var durationMinutes: Int,
    var attributes: MutableMap<String, String>,
    var status: ScheduledLockerReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLockerReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLockerReviewStatus): Boolean {
        when (status) {
            ScheduledLockerReviewStatus.OPEN -> return next == ScheduledLockerReviewStatus.IN_REVIEW || next == ScheduledLockerReviewStatus.REOPENED
            ScheduledLockerReviewStatus.IN_REVIEW -> return next == ScheduledLockerReviewStatus.RESOLVED
            ScheduledLockerReviewStatus.RESOLVED -> {
                when (next) {
                    ScheduledLockerReviewStatus.REOPENED, ScheduledLockerReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledLockerReviewStatus.REOPENED -> return next == ScheduledLockerReviewStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledLockerReviewStatus) {
        if (!canMoveTo(next)) {
            error("scheduled locker review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round(amount * sequence.toDouble() * 100) / 100

    fun snapshot(): ScheduledLockerReview {
        return ScheduledLockerReview(
            code = code,
            name = name,
            description = description,
            amount = amount,
            phone = phone,
            checksum = checksum,
            sequence = sequence,
            driverId = driverId,
            durationMinutes = durationMinutes,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled locker review in status open. */
fun newScheduledLockerReview(code: String, name: String): ScheduledLockerReview {
    return ScheduledLockerReview(
        code = code,
        name = name,
        description = "",
        amount = 0.0,
        phone = "",
        checksum = "",
        sequence = 0,
        driverId = "",
        durationMinutes = 0,
        attributes = mutableMapOf<String, String>(),
        status = ScheduledLockerReviewStatus.OPEN,
    )
}
