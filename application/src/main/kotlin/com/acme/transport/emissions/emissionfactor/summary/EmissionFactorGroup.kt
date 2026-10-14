package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor group is in its life cycle. */
enum class EmissionFactorGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a emission factor group and its current status. */
data class EmissionFactorGroup(
    var code: String,
    var label: String,
    var revision: Int,
    var endsAt: Int,
    var attempts: Int,
    var approved: Boolean,
    var notes: String,
    var score: Double,
    var version: Int,
    var stopCount: Int,
    var threshold: Double,
    var status: EmissionFactorGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorGroupStatus): Boolean {
        when (status) {
            EmissionFactorGroupStatus.REQUESTED -> {
                when (next) {
                    EmissionFactorGroupStatus.GRANTED, EmissionFactorGroupStatus.REVOKED -> return true
                }
                return false
            }
            EmissionFactorGroupStatus.GRANTED -> {
                when (next) {
                    EmissionFactorGroupStatus.REVOKED, EmissionFactorGroupStatus.EXPIRED -> return true
                }
                return false
            }
            EmissionFactorGroupStatus.REVOKED -> return next == EmissionFactorGroupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: EmissionFactorGroupStatus) {
        if (!canMoveTo(next)) {
            error("emission factor group $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round(version.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): EmissionFactorGroup {
        return EmissionFactorGroup(
            code = code,
            label = label,
            revision = revision,
            endsAt = endsAt,
            attempts = attempts,
            approved = approved,
            notes = notes,
            score = score,
            version = version,
            stopCount = stopCount,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a emission factor group in status requested. */
fun newEmissionFactorGroup(code: String, label: String): EmissionFactorGroup {
    return EmissionFactorGroup(
        code = code,
        label = label,
        revision = 0,
        endsAt = 0,
        attempts = 0,
        approved = false,
        notes = "",
        score = 0.0,
        version = 0,
        stopCount = 0,
        threshold = 0.0,
        status = EmissionFactorGroupStatus.REQUESTED,
    )
}
