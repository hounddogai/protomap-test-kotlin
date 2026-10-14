package com.acme.transport.freightpay.ratecard.draft

import com.acme.transport.kit.text.normalize

/** Where a rate card template is in its life cycle. */
enum class RateCardTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a rate card template and its current status. */
data class RateCardTemplate(
    var code: String,
    var label: String,
    var name: String,
    var version: Int,
    var summary: String,
    var locale: String,
    var status: RateCardTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardTemplateStatus): Boolean {
        when (status) {
            RateCardTemplateStatus.NEW -> return next == RateCardTemplateStatus.QUEUED
            RateCardTemplateStatus.QUEUED -> {
                when (next) {
                    RateCardTemplateStatus.RUNNING, RateCardTemplateStatus.DONE -> return true
                }
                return false
            }
            RateCardTemplateStatus.RUNNING -> return next == RateCardTemplateStatus.DONE || next == RateCardTemplateStatus.FAILED
            RateCardTemplateStatus.DONE -> {
                when (next) {
                    RateCardTemplateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardTemplateStatus) {
        if (!canMoveTo(next)) {
            error("rate card template $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): RateCardTemplate {
        return RateCardTemplate(
            code = code,
            label = label,
            name = name,
            version = version,
            summary = summary,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rate card template in status new. */
fun newRateCardTemplate(code: String, label: String): RateCardTemplate {
    return RateCardTemplate(
        code = code,
        label = label,
        name = "",
        version = 0,
        summary = "",
        locale = "",
        status = RateCardTemplateStatus.NEW,
    )
}
