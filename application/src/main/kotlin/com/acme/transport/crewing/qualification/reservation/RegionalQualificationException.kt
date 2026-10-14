package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional qualification exception is in its life cycle. */
enum class RegionalQualificationExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a regional qualification exception and its current status. */
data class RegionalQualificationException(
    var code: String,
    var label: String,
    var createdAt: Int,
    var attributes: MutableMap<String, String>,
    var ceiling: Double,
    var pinned: Boolean,
    var status: RegionalQualificationExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalQualificationExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalQualificationExceptionStatus): Boolean {
        when (status) {
            RegionalQualificationExceptionStatus.DRAFT -> return next == RegionalQualificationExceptionStatus.ACTIVE || next == RegionalQualificationExceptionStatus.SUSPENDED
            RegionalQualificationExceptionStatus.PENDING -> return next == RegionalQualificationExceptionStatus.ACTIVE
            RegionalQualificationExceptionStatus.ACTIVE -> return next == RegionalQualificationExceptionStatus.SUSPENDED || next == RegionalQualificationExceptionStatus.CLOSED
            RegionalQualificationExceptionStatus.SUSPENDED -> return next == RegionalQualificationExceptionStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalQualificationExceptionStatus) {
        if (!canMoveTo(next)) {
            error("regional qualification exception $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round((ceiling + createdAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalQualificationException {
        return RegionalQualificationException(
            code = code,
            label = label,
            createdAt = createdAt,
            attributes = attributes,
            ceiling = ceiling,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional qualification exception in status draft. */
fun newRegionalQualificationException(code: String, label: String): RegionalQualificationException {
    return RegionalQualificationException(
        code = code,
        label = label,
        createdAt = 0,
        attributes = mutableMapOf<String, String>(),
        ceiling = 0.0,
        pinned = false,
        status = RegionalQualificationExceptionStatus.DRAFT,
    )
}
