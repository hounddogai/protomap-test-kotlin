package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.kit.text.normalize

/** Where a emission factor checkpoint is in its life cycle. */
enum class EmissionFactorCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A emission factor checkpoint as the emission factor team keeps it. */
data class EmissionFactorCheckpoint(
    var code: String,
    var label: String,
    var title: String,
    var reviewer: String,
    var phone: String,
    var quantity: Int,
    var status: EmissionFactorCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorCheckpointStatus): Boolean {
        when (status) {
            EmissionFactorCheckpointStatus.REQUESTED -> return next == EmissionFactorCheckpointStatus.REVOKED || next == EmissionFactorCheckpointStatus.EXPIRED
            EmissionFactorCheckpointStatus.GRANTED -> {
                when (next) {
                    EmissionFactorCheckpointStatus.REVOKED -> return true
                }
                return false
            }
            EmissionFactorCheckpointStatus.REVOKED -> return next == EmissionFactorCheckpointStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: EmissionFactorCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("emission factor checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): EmissionFactorCheckpoint {
        return EmissionFactorCheckpoint(
            code = code,
            label = label,
            title = title,
            reviewer = reviewer,
            phone = phone,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a emission factor checkpoint in status requested. */
fun newEmissionFactorCheckpoint(code: String, label: String): EmissionFactorCheckpoint {
    return EmissionFactorCheckpoint(
        code = code,
        label = label,
        title = "",
        reviewer = "",
        phone = "",
        quantity = 0,
        status = EmissionFactorCheckpointStatus.REQUESTED,
    )
}
