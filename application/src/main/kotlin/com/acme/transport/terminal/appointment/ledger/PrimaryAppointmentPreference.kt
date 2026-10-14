package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary appointment preference is in its life cycle. */
enum class PrimaryAppointmentPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a primary appointment preference and its current status. */
data class PrimaryAppointmentPreference(
    var code: String,
    var label: String,
    var phone: String,
    var unit: String,
    var amount: Double,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var revision: Int,
    var tags: MutableList<String>,
    var status: PrimaryAppointmentPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAppointmentPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAppointmentPreferenceStatus): Boolean {
        when (status) {
            PrimaryAppointmentPreferenceStatus.PROPOSED -> return next == PrimaryAppointmentPreferenceStatus.APPROVED || next == PrimaryAppointmentPreferenceStatus.WITHDRAWN
            PrimaryAppointmentPreferenceStatus.APPROVED -> {
                when (next) {
                    PrimaryAppointmentPreferenceStatus.REJECTED, PrimaryAppointmentPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryAppointmentPreferenceStatus.REJECTED -> return next == PrimaryAppointmentPreferenceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryAppointmentPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("primary appointment preference $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round((amount - revision.toDouble()) * 100) / 100

    fun snapshot(): PrimaryAppointmentPreference {
        return PrimaryAppointmentPreference(
            code = code,
            label = label,
            phone = phone,
            unit = unit,
            amount = amount,
            attributes = attributes,
            summary = summary,
            revision = revision,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary appointment preference in status proposed. */
fun newPrimaryAppointmentPreference(code: String, label: String): PrimaryAppointmentPreference {
    return PrimaryAppointmentPreference(
        code = code,
        label = label,
        phone = "",
        unit = "",
        amount = 0.0,
        attributes = mutableMapOf<String, String>(),
        summary = "",
        revision = 0,
        tags = mutableListOf<String>(),
        status = PrimaryAppointmentPreferenceStatus.PROPOSED,
    )
}
