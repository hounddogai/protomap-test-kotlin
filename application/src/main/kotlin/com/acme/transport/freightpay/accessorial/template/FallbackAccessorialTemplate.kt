package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback accessorial template records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a fallback accessorial template is in its life cycle. */
enum class FallbackAccessorialTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback accessorial template as the accessorial team keeps it. */
data class FallbackAccessorialTemplate(
    var code: String,
    var label: String,
    var externalRef: String,
    var threshold: Double,
    var summary: String,
    var reviewer: String,
    var loadKg: Double,
    var status: FallbackAccessorialTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAccessorialTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAccessorialTemplateStatus): Boolean {
        when (status) {
            FallbackAccessorialTemplateStatus.NEW -> {
                when (next) {
                    FallbackAccessorialTemplateStatus.QUEUED, FallbackAccessorialTemplateStatus.RUNNING -> return true
                }
                return false
            }
            FallbackAccessorialTemplateStatus.QUEUED -> return next == FallbackAccessorialTemplateStatus.DONE || next == FallbackAccessorialTemplateStatus.FAILED
            FallbackAccessorialTemplateStatus.RUNNING -> {
                when (next) {
                    FallbackAccessorialTemplateStatus.DONE, FallbackAccessorialTemplateStatus.FAILED -> return true
                }
                return false
            }
            FallbackAccessorialTemplateStatus.DONE -> return next == FallbackAccessorialTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackAccessorialTemplateStatus) {
        if (!canMoveTo(next)) {
            error("fallback accessorial template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback accessorial template"} $code"

    fun adjustedValue(): Double = round(threshold * loadKg * 100) / 100

    fun snapshot(): FallbackAccessorialTemplate {
        return FallbackAccessorialTemplate(
            code = code,
            label = label,
            externalRef = externalRef,
            threshold = threshold,
            summary = summary,
            reviewer = reviewer,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback accessorial template in status new. */
fun newFallbackAccessorialTemplate(code: String, label: String): FallbackAccessorialTemplate {
    return FallbackAccessorialTemplate(
        code = code,
        label = label,
        externalRef = "",
        threshold = 0.0,
        summary = "",
        reviewer = "",
        loadKg = 0.0,
        status = FallbackAccessorialTemplateStatus.NEW,
    )
}
