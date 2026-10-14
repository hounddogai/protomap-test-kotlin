package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional tender template is in its life cycle. */
enum class RegionalTenderTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional tender template, with what the planners need to act on it. */
data class RegionalTenderTemplate(
    var code: String,
    var title: String,
    var carrier: String,
    var budget: Double,
    var startsAt: Int,
    var segment: String,
    var floorValue: Double,
    var timezone: String,
    var status: RegionalTenderTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTenderTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTenderTemplateStatus): Boolean {
        when (status) {
            RegionalTenderTemplateStatus.NEW -> {
                when (next) {
                    RegionalTenderTemplateStatus.QUEUED -> return true
                }
                return false
            }
            RegionalTenderTemplateStatus.QUEUED -> {
                when (next) {
                    RegionalTenderTemplateStatus.RUNNING, RegionalTenderTemplateStatus.FAILED -> return true
                }
                return false
            }
            RegionalTenderTemplateStatus.RUNNING -> {
                when (next) {
                    RegionalTenderTemplateStatus.DONE, RegionalTenderTemplateStatus.FAILED -> return true
                }
                return false
            }
            RegionalTenderTemplateStatus.DONE -> {
                when (next) {
                    RegionalTenderTemplateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalTenderTemplateStatus) {
        if (!canMoveTo(next)) {
            error("regional tender template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveTotal(): Double = round((budget + startsAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalTenderTemplate {
        return RegionalTenderTemplate(
            code = code,
            title = title,
            carrier = carrier,
            budget = budget,
            startsAt = startsAt,
            segment = segment,
            floorValue = floorValue,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional tender template in status new. */
fun newRegionalTenderTemplate(code: String, title: String): RegionalTenderTemplate {
    return RegionalTenderTemplate(
        code = code,
        title = title,
        carrier = "",
        budget = 0.0,
        startsAt = 0,
        segment = "",
        floorValue = 0.0,
        timezone = "",
        status = RegionalTenderTemplateStatus.NEW,
    )
}
