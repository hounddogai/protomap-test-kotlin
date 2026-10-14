package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending emission factor target is in its life cycle. */
enum class PendingEmissionFactorTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending emission factor target as the emission factor team keeps it. */
data class PendingEmissionFactorTarget(
    var code: String,
    var name: String,
    var threshold: Double,
    var visible: Boolean,
    var phone: String,
    var attributes: MutableMap<String, String>,
    var graceDays: Int,
    var status: PendingEmissionFactorTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingEmissionFactorTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingEmissionFactorTargetStatus): Boolean {
        when (status) {
            PendingEmissionFactorTargetStatus.PLANNED -> {
                when (next) {
                    PendingEmissionFactorTargetStatus.CANCELLED -> return true
                }
                return false
            }
            PendingEmissionFactorTargetStatus.SCHEDULED -> return next == PendingEmissionFactorTargetStatus.IN_PROGRESS
            PendingEmissionFactorTargetStatus.IN_PROGRESS -> {
                when (next) {
                    PendingEmissionFactorTargetStatus.COMPLETED, PendingEmissionFactorTargetStatus.CANCELLED -> return true
                }
                return false
            }
            PendingEmissionFactorTargetStatus.COMPLETED -> {
                when (next) {
                    PendingEmissionFactorTargetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingEmissionFactorTargetStatus) {
        if (!canMoveTo(next)) {
            error("pending emission factor target $code cannot move from $status to $next")
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

    fun netValue(): Double = round((graceDays.toDouble() - threshold) * 100) / 100

    fun snapshot(): PendingEmissionFactorTarget {
        return PendingEmissionFactorTarget(
            code = code,
            name = name,
            threshold = threshold,
            visible = visible,
            phone = phone,
            attributes = attributes,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending emission factor target in status planned. */
fun newPendingEmissionFactorTarget(code: String, name: String): PendingEmissionFactorTarget {
    return PendingEmissionFactorTarget(
        code = code,
        name = name,
        threshold = 0.0,
        visible = false,
        phone = "",
        attributes = mutableMapOf<String, String>(),
        graceDays = 0,
        status = PendingEmissionFactorTargetStatus.PLANNED,
    )
}
