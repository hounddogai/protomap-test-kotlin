package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped emission factor draft is in its life cycle. */
enum class CappedEmissionFactorDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped emission factor draft as the emission factor team keeps it. */
data class CappedEmissionFactorDraft(
    var code: String,
    var label: String,
    var maximum: Double,
    var version: Int,
    var driverId: String,
    var status: CappedEmissionFactorDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedEmissionFactorDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedEmissionFactorDraftStatus): Boolean {
        when (status) {
            CappedEmissionFactorDraftStatus.REQUESTED -> return next == CappedEmissionFactorDraftStatus.GRANTED
            CappedEmissionFactorDraftStatus.GRANTED -> {
                when (next) {
                    CappedEmissionFactorDraftStatus.EXPIRED -> return true
                }
                return false
            }
            CappedEmissionFactorDraftStatus.REVOKED -> return next == CappedEmissionFactorDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedEmissionFactorDraftStatus) {
        if (!canMoveTo(next)) {
            error("capped emission factor draft $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveVolume(): Double = round((version.toDouble() - maximum) * 100) / 100

    fun snapshot(): CappedEmissionFactorDraft {
        return CappedEmissionFactorDraft(
            code = code,
            label = label,
            maximum = maximum,
            version = version,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped emission factor draft in status requested. */
fun newCappedEmissionFactorDraft(code: String, label: String): CappedEmissionFactorDraft {
    return CappedEmissionFactorDraft(
        code = code,
        label = label,
        maximum = 0.0,
        version = 0,
        driverId = "",
        status = CappedEmissionFactorDraftStatus.REQUESTED,
    )
}
