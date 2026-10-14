package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor notice records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor notice is in its life cycle. */
enum class EmissionFactorNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a emission factor notice and its current status. */
data class EmissionFactorNotice(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var windowDays: Int,
    var score: Double,
    var status: EmissionFactorNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorNoticeStatus): Boolean {
        when (status) {
            EmissionFactorNoticeStatus.OPEN -> return next == EmissionFactorNoticeStatus.RESOLVED || next == EmissionFactorNoticeStatus.REOPENED
            EmissionFactorNoticeStatus.IN_REVIEW -> return next == EmissionFactorNoticeStatus.RESOLVED || next == EmissionFactorNoticeStatus.REOPENED
            EmissionFactorNoticeStatus.RESOLVED -> {
                when (next) {
                    EmissionFactorNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            EmissionFactorNoticeStatus.REOPENED -> {
                when (next) {
                    EmissionFactorNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorNoticeStatus) {
        if (!canMoveTo(next)) {
            error("emission factor notice $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round((updatedAt.toDouble() + score) * 100) / 100

    fun snapshot(): EmissionFactorNotice {
        return EmissionFactorNotice(
            code = code,
            label = label,
            updatedAt = updatedAt,
            windowDays = windowDays,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a emission factor notice in status open. */
fun newEmissionFactorNotice(code: String, label: String): EmissionFactorNotice {
    return EmissionFactorNotice(
        code = code,
        label = label,
        updatedAt = 0,
        windowDays = 0,
        score = 0.0,
        status = EmissionFactorNoticeStatus.OPEN,
    )
}
