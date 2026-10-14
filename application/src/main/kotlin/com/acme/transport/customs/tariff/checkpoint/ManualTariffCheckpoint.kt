package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual tariff checkpoint is in its life cycle. */
enum class ManualTariffCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual tariff checkpoint as the tariff team keeps it. */
data class ManualTariffCheckpoint(
    var code: String,
    var title: String,
    var margin: Double,
    var score: Double,
    var name: String,
    var status: ManualTariffCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTariffCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTariffCheckpointStatus): Boolean {
        when (status) {
            ManualTariffCheckpointStatus.DRAFT -> {
                when (next) {
                    ManualTariffCheckpointStatus.ACTIVE, ManualTariffCheckpointStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualTariffCheckpointStatus.PENDING -> {
                when (next) {
                    ManualTariffCheckpointStatus.ACTIVE, ManualTariffCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            ManualTariffCheckpointStatus.ACTIVE -> {
                when (next) {
                    ManualTariffCheckpointStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualTariffCheckpointStatus.SUSPENDED -> return next == ManualTariffCheckpointStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualTariffCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("manual tariff checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netLoad(): Double = round(score * margin * 100) / 100

    fun snapshot(): ManualTariffCheckpoint {
        return ManualTariffCheckpoint(
            code = code,
            title = title,
            margin = margin,
            score = score,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual tariff checkpoint in status draft. */
fun newManualTariffCheckpoint(code: String, title: String): ManualTariffCheckpoint {
    return ManualTariffCheckpoint(
        code = code,
        title = title,
        margin = 0.0,
        score = 0.0,
        name = "",
        status = ManualTariffCheckpointStatus.DRAFT,
    )
}
