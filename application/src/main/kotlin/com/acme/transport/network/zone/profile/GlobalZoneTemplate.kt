package com.acme.transport.network.zone.profile

import com.acme.transport.kit.text.normalize

/** Where a global zone template is in its life cycle. */
enum class GlobalZoneTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global zone template as the zone team keeps it. */
data class GlobalZoneTemplate(
    var code: String,
    var title: String,
    var shiftCode: String,
    var loadKg: Double,
    var checksum: String,
    var status: GlobalZoneTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalZoneTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalZoneTemplateStatus): Boolean {
        when (status) {
            GlobalZoneTemplateStatus.DRAFT -> {
                when (next) {
                    GlobalZoneTemplateStatus.ACTIVE, GlobalZoneTemplateStatus.CLOSED -> return true
                }
                return false
            }
            GlobalZoneTemplateStatus.PENDING -> return next == GlobalZoneTemplateStatus.ACTIVE || next == GlobalZoneTemplateStatus.CLOSED
            GlobalZoneTemplateStatus.ACTIVE -> return next == GlobalZoneTemplateStatus.CLOSED
            GlobalZoneTemplateStatus.SUSPENDED -> {
                when (next) {
                    GlobalZoneTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalZoneTemplateStatus) {
        if (!canMoveTo(next)) {
            error("global zone template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = loadKg

    fun snapshot(): GlobalZoneTemplate {
        return GlobalZoneTemplate(
            code = code,
            title = title,
            shiftCode = shiftCode,
            loadKg = loadKg,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global zone template in status draft. */
fun newGlobalZoneTemplate(code: String, title: String): GlobalZoneTemplate {
    return GlobalZoneTemplate(
        code = code,
        title = title,
        shiftCode = "",
        loadKg = 0.0,
        checksum = "",
        status = GlobalZoneTemplateStatus.DRAFT,
    )
}
