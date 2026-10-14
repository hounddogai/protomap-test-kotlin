package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.text.normalize

/** Where a seasonal roster template is in its life cycle. */
enum class SeasonalRosterTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal roster template. Its code is unique within a tenant. */
data class SeasonalRosterTemplate(
    var code: String,
    var label: String,
    var country: String,
    var phone: String,
    var externalRef: String,
    var channel: String,
    var timezone: String,
    var etaMinutes: Int,
    var status: SeasonalRosterTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRosterTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRosterTemplateStatus): Boolean {
        when (status) {
            SeasonalRosterTemplateStatus.REQUESTED -> return next == SeasonalRosterTemplateStatus.GRANTED || next == SeasonalRosterTemplateStatus.EXPIRED
            SeasonalRosterTemplateStatus.GRANTED -> {
                when (next) {
                    SeasonalRosterTemplateStatus.REVOKED -> return true
                }
                return false
            }
            SeasonalRosterTemplateStatus.REVOKED -> return next == SeasonalRosterTemplateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalRosterTemplateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal roster template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): SeasonalRosterTemplate {
        return SeasonalRosterTemplate(
            code = code,
            label = label,
            country = country,
            phone = phone,
            externalRef = externalRef,
            channel = channel,
            timezone = timezone,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal roster template in status requested. */
fun newSeasonalRosterTemplate(code: String, label: String): SeasonalRosterTemplate {
    return SeasonalRosterTemplate(
        code = code,
        label = label,
        country = "",
        phone = "",
        externalRef = "",
        channel = "",
        timezone = "",
        etaMinutes = 0,
        status = SeasonalRosterTemplateStatus.REQUESTED,
    )
}
