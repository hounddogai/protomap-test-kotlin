package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.text.normalize

/** Where a zone template is in its life cycle. */
enum class ZoneTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a zone template and its current status. */
data class ZoneTemplate(
    var code: String,
    var name: String,
    var country: String,
    var externalRef: String,
    var source: String,
    var shiftCode: String,
    var priority: Int,
    var dock: String,
    var driverId: String,
    var channel: String,
    var status: ZoneTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneTemplateStatus): Boolean {
        when (status) {
            ZoneTemplateStatus.CREATED -> {
                when (next) {
                    ZoneTemplateStatus.VALIDATED, ZoneTemplateStatus.PUBLISHED -> return true
                }
                return false
            }
            ZoneTemplateStatus.VALIDATED -> return next == ZoneTemplateStatus.PUBLISHED
            ZoneTemplateStatus.PUBLISHED -> return next == ZoneTemplateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ZoneTemplateStatus) {
        if (!canMoveTo(next)) {
            error("zone template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): ZoneTemplate {
        return ZoneTemplate(
            code = code,
            name = name,
            country = country,
            externalRef = externalRef,
            source = source,
            shiftCode = shiftCode,
            priority = priority,
            dock = dock,
            driverId = driverId,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a zone template in status created. */
fun newZoneTemplate(code: String, name: String): ZoneTemplate {
    return ZoneTemplate(
        code = code,
        name = name,
        country = "",
        externalRef = "",
        source = "",
        shiftCode = "",
        priority = 0,
        dock = "",
        driverId = "",
        channel = "",
        status = ZoneTemplateStatus.CREATED,
    )
}
