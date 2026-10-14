package com.acme.transport.customs.inspection.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default inspection profile records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a default inspection profile is in its life cycle. */
enum class DefaultInspectionProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default inspection profile as the inspection team keeps it. */
data class DefaultInspectionProfile(
    var code: String,
    var label: String,
    var revision: Int,
    var ownerId: String,
    var capacity: Int,
    var rate: Double,
    var trailerId: String,
    var retries: Int,
    var dock: String,
    var fuelLiters: Double,
    var source: String,
    var status: DefaultInspectionProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultInspectionProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultInspectionProfileStatus): Boolean {
        when (status) {
            DefaultInspectionProfileStatus.PROPOSED -> {
                when (next) {
                    DefaultInspectionProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultInspectionProfileStatus.APPROVED -> {
                when (next) {
                    DefaultInspectionProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultInspectionProfileStatus.REJECTED -> {
                when (next) {
                    DefaultInspectionProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultInspectionProfileStatus) {
        if (!canMoveTo(next)) {
            error("default inspection profile $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveVolume(): Double = round(revision.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): DefaultInspectionProfile {
        return DefaultInspectionProfile(
            code = code,
            label = label,
            revision = revision,
            ownerId = ownerId,
            capacity = capacity,
            rate = rate,
            trailerId = trailerId,
            retries = retries,
            dock = dock,
            fuelLiters = fuelLiters,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default inspection profile in status proposed. */
fun newDefaultInspectionProfile(code: String, label: String): DefaultInspectionProfile {
    return DefaultInspectionProfile(
        code = code,
        label = label,
        revision = 0,
        ownerId = "",
        capacity = 0,
        rate = 0.0,
        trailerId = "",
        retries = 0,
        dock = "",
        fuelLiters = 0.0,
        source = "",
        status = DefaultInspectionProfileStatus.PROPOSED,
    )
}
