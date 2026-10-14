package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident audit is in its life cycle. */
enum class IncidentAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a incident audit and its current status. */
data class IncidentAudit(
    var code: String,
    var label: String,
    var category: String,
    var summary: String,
    var quantity: Int,
    var pinned: Boolean,
    var attributes: MutableMap<String, String>,
    var checksum: String,
    var odometer: Int,
    var status: IncidentAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentAuditStatus): Boolean {
        when (status) {
            IncidentAuditStatus.NEW -> return next == IncidentAuditStatus.DONE || next == IncidentAuditStatus.FAILED
            IncidentAuditStatus.QUEUED -> return next == IncidentAuditStatus.DONE
            IncidentAuditStatus.RUNNING -> {
                when (next) {
                    IncidentAuditStatus.DONE, IncidentAuditStatus.FAILED -> return true
                }
                return false
            }
            IncidentAuditStatus.DONE -> {
                when (next) {
                    IncidentAuditStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentAuditStatus) {
        if (!canMoveTo(next)) {
            error("incident audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"incident audit"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((quantity.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): IncidentAudit {
        return IncidentAudit(
            code = code,
            label = label,
            category = category,
            summary = summary,
            quantity = quantity,
            pinned = pinned,
            attributes = attributes,
            checksum = checksum,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a incident audit in status new. */
fun newIncidentAudit(code: String, label: String): IncidentAudit {
    return IncidentAudit(
        code = code,
        label = label,
        category = "",
        summary = "",
        quantity = 0,
        pinned = false,
        attributes = mutableMapOf<String, String>(),
        checksum = "",
        odometer = 0,
        status = IncidentAuditStatus.NEW,
    )
}
