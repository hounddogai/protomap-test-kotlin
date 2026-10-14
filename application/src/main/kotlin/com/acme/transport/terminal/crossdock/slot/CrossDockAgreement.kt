package com.acme.transport.terminal.crossdock.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock agreement is in its life cycle. */
enum class CrossDockAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a cross dock agreement and its current status. */
data class CrossDockAgreement(
    var code: String,
    var name: String,
    var budget: Double,
    var capacity: Int,
    var active: Boolean,
    var source: String,
    var summary: String,
    var graceDays: Int,
    var pinned: Boolean,
    var unit: String,
    var attributes: MutableMap<String, String>,
    var status: CrossDockAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockAgreementStatus): Boolean {
        when (status) {
            CrossDockAgreementStatus.OPEN -> return next == CrossDockAgreementStatus.RESOLVED || next == CrossDockAgreementStatus.ARCHIVED
            CrossDockAgreementStatus.IN_REVIEW -> return next == CrossDockAgreementStatus.REOPENED
            CrossDockAgreementStatus.RESOLVED -> return next == CrossDockAgreementStatus.ARCHIVED
            CrossDockAgreementStatus.REOPENED -> return next == CrossDockAgreementStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CrossDockAgreementStatus) {
        if (!canMoveTo(next)) {
            error("cross dock agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((capacity.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): CrossDockAgreement {
        return CrossDockAgreement(
            code = code,
            name = name,
            budget = budget,
            capacity = capacity,
            active = active,
            source = source,
            summary = summary,
            graceDays = graceDays,
            pinned = pinned,
            unit = unit,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cross dock agreement in status open. */
fun newCrossDockAgreement(code: String, name: String): CrossDockAgreement {
    return CrossDockAgreement(
        code = code,
        name = name,
        budget = 0.0,
        capacity = 0,
        active = false,
        source = "",
        summary = "",
        graceDays = 0,
        pinned = false,
        unit = "",
        attributes = mutableMapOf<String, String>(),
        status = CrossDockAgreementStatus.OPEN,
    )
}
