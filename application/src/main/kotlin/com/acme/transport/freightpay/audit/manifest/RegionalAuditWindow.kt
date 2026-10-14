package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional audit window is in its life cycle. */
enum class RegionalAuditWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional audit window. Its code is unique within a tenant. */
data class RegionalAuditWindow(
    var code: String,
    var name: String,
    var vehicleId: String,
    var endsAt: Int,
    var externalRef: String,
    var title: String,
    var expiresAt: Int,
    var label: String,
    var status: RegionalAuditWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAuditWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAuditWindowStatus): Boolean {
        when (status) {
            RegionalAuditWindowStatus.CREATED -> return next == RegionalAuditWindowStatus.VALIDATED
            RegionalAuditWindowStatus.VALIDATED -> return next == RegionalAuditWindowStatus.PUBLISHED
            RegionalAuditWindowStatus.PUBLISHED -> return next == RegionalAuditWindowStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalAuditWindowStatus) {
        if (!canMoveTo(next)) {
            error("regional audit window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"regional audit window"} $code"

    fun netVolume(): Double = round(expiresAt.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): RegionalAuditWindow {
        return RegionalAuditWindow(
            code = code,
            name = name,
            vehicleId = vehicleId,
            endsAt = endsAt,
            externalRef = externalRef,
            title = title,
            expiresAt = expiresAt,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional audit window in status created. */
fun newRegionalAuditWindow(code: String, name: String): RegionalAuditWindow {
    return RegionalAuditWindow(
        code = code,
        name = name,
        vehicleId = "",
        endsAt = 0,
        externalRef = "",
        title = "",
        expiresAt = 0,
        label = "",
        status = RegionalAuditWindowStatus.CREATED,
    )
}
