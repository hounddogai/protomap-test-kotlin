package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor draft records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor draft is in its life cycle. */
enum class EmissionFactorDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A emission factor draft as the emission factor team keeps it. */
data class EmissionFactorDraft(
    var code: String,
    var label: String,
    var currency: String,
    var unit: String,
    var discount: Double,
    var batchSize: Int,
    var status: EmissionFactorDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorDraftStatus): Boolean {
        when (status) {
            EmissionFactorDraftStatus.NEW -> return next == EmissionFactorDraftStatus.RUNNING || next == EmissionFactorDraftStatus.DONE
            EmissionFactorDraftStatus.QUEUED -> return next == EmissionFactorDraftStatus.FAILED
            EmissionFactorDraftStatus.RUNNING -> return next == EmissionFactorDraftStatus.FAILED
            EmissionFactorDraftStatus.DONE -> return next == EmissionFactorDraftStatus.FAILED
        }
        return false
    }

    fun moveTo(next: EmissionFactorDraftStatus) {
        if (!canMoveTo(next)) {
            error("emission factor draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netVolume(): Double = round((discount - batchSize.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorDraft {
        return EmissionFactorDraft(
            code = code,
            label = label,
            currency = currency,
            unit = unit,
            discount = discount,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a emission factor draft in status new. */
fun newEmissionFactorDraft(code: String, label: String): EmissionFactorDraft {
    return EmissionFactorDraft(
        code = code,
        label = label,
        currency = "",
        unit = "",
        discount = 0.0,
        batchSize = 0,
        status = EmissionFactorDraftStatus.NEW,
    )
}
