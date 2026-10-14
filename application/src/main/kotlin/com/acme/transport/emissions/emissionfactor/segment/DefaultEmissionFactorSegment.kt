package com.acme.transport.emissions.emissionfactor.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default emission factor segment records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a default emission factor segment is in its life cycle. */
enum class DefaultEmissionFactorSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default emission factor segment as the emission factor team keeps it. */
data class DefaultEmissionFactorSegment(
    var code: String,
    var label: String,
    var endsAt: Int,
    var loadKg: Double,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var name: String,
    var status: DefaultEmissionFactorSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultEmissionFactorSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultEmissionFactorSegmentStatus): Boolean {
        when (status) {
            DefaultEmissionFactorSegmentStatus.REQUESTED -> {
                when (next) {
                    DefaultEmissionFactorSegmentStatus.REVOKED, DefaultEmissionFactorSegmentStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultEmissionFactorSegmentStatus.GRANTED -> return next == DefaultEmissionFactorSegmentStatus.EXPIRED
            DefaultEmissionFactorSegmentStatus.REVOKED -> {
                when (next) {
                    DefaultEmissionFactorSegmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultEmissionFactorSegmentStatus) {
        if (!canMoveTo(next)) {
            error("default emission factor segment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default emission factor segment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((endsAt.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): DefaultEmissionFactorSegment {
        return DefaultEmissionFactorSegment(
            code = code,
            label = label,
            endsAt = endsAt,
            loadKg = loadKg,
            odometer = odometer,
            attributes = attributes,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default emission factor segment in status requested. */
fun newDefaultEmissionFactorSegment(code: String, label: String): DefaultEmissionFactorSegment {
    return DefaultEmissionFactorSegment(
        code = code,
        label = label,
        endsAt = 0,
        loadKg = 0.0,
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        name = "",
        status = DefaultEmissionFactorSegmentStatus.REQUESTED,
    )
}
