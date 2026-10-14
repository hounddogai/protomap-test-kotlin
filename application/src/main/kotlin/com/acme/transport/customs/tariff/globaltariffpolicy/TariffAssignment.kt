package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff assignment is in its life cycle. */
enum class TariffAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tariff assignment. Its code is unique within a tenant. */
data class TariffAssignment(
    var code: String,
    var name: String,
    var segment: String,
    var visible: Boolean,
    var margin: Double,
    var attributes: MutableMap<String, String>,
    var discount: Double,
    var status: TariffAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffAssignmentStatus): Boolean {
        when (status) {
            TariffAssignmentStatus.IDLE -> return next == TariffAssignmentStatus.READY || next == TariffAssignmentStatus.STOPPED
            TariffAssignmentStatus.WARMING -> return next == TariffAssignmentStatus.READY || next == TariffAssignmentStatus.STOPPED
            TariffAssignmentStatus.READY -> {
                when (next) {
                    TariffAssignmentStatus.DRAINING, TariffAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            TariffAssignmentStatus.DRAINING -> {
                when (next) {
                    TariffAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("tariff assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((discount + margin) * 100) / 100

    fun snapshot(): TariffAssignment {
        return TariffAssignment(
            code = code,
            name = name,
            segment = segment,
            visible = visible,
            margin = margin,
            attributes = attributes,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tariff assignment in status idle. */
fun newTariffAssignment(code: String, name: String): TariffAssignment {
    return TariffAssignment(
        code = code,
        name = name,
        segment = "",
        visible = false,
        margin = 0.0,
        attributes = mutableMapOf<String, String>(),
        discount = 0.0,
        status = TariffAssignmentStatus.IDLE,
    )
}
