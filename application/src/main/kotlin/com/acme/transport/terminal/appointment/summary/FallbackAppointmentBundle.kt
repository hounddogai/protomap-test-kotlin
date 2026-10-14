package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback appointment bundle is in its life cycle. */
enum class FallbackAppointmentBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fallback appointment bundle, with what the planners need to act on it. */
data class FallbackAppointmentBundle(
    var code: String,
    var title: String,
    var ratio: Double,
    var createdAt: Int,
    var segment: String,
    var limit: Int,
    var quantity: Int,
    var notes: String,
    var approved: Boolean,
    var category: String,
    var status: FallbackAppointmentBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAppointmentBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAppointmentBundleStatus): Boolean {
        when (status) {
            FallbackAppointmentBundleStatus.PROPOSED -> return next == FallbackAppointmentBundleStatus.APPROVED || next == FallbackAppointmentBundleStatus.REJECTED
            FallbackAppointmentBundleStatus.APPROVED -> return next == FallbackAppointmentBundleStatus.REJECTED
            FallbackAppointmentBundleStatus.REJECTED -> return next == FallbackAppointmentBundleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackAppointmentBundleStatus) {
        if (!canMoveTo(next)) {
            error("fallback appointment bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedValue(): Double = round((ratio + quantity.toDouble()) * 100) / 100

    fun snapshot(): FallbackAppointmentBundle {
        return FallbackAppointmentBundle(
            code = code,
            title = title,
            ratio = ratio,
            createdAt = createdAt,
            segment = segment,
            limit = limit,
            quantity = quantity,
            notes = notes,
            approved = approved,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback appointment bundle in status proposed. */
fun newFallbackAppointmentBundle(code: String, title: String): FallbackAppointmentBundle {
    return FallbackAppointmentBundle(
        code = code,
        title = title,
        ratio = 0.0,
        createdAt = 0,
        segment = "",
        limit = 0,
        quantity = 0,
        notes = "",
        approved = false,
        category = "",
        status = FallbackAppointmentBundleStatus.PROPOSED,
    )
}
