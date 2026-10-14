package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff checkpoint records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a tariff checkpoint is in its life cycle. */
enum class TariffCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tariff checkpoint. Its code is unique within a tenant. */
data class TariffCheckpoint(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var quantity: Int,
    var pinned: Boolean,
    var attributes: MutableMap<String, String>,
    var durationMinutes: Int,
    var maximum: Double,
    var country: String,
    var status: TariffCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffCheckpointStatus): Boolean {
        when (status) {
            TariffCheckpointStatus.IDLE -> {
                when (next) {
                    TariffCheckpointStatus.WARMING, TariffCheckpointStatus.DRAINING -> return true
                }
                return false
            }
            TariffCheckpointStatus.WARMING -> {
                when (next) {
                    TariffCheckpointStatus.READY, TariffCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            TariffCheckpointStatus.READY -> return next == TariffCheckpointStatus.STOPPED
            TariffCheckpointStatus.DRAINING -> {
                when (next) {
                    TariffCheckpointStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("tariff checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tariff checkpoint"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round(quantity.toDouble() * maximum * 100) / 100

    fun snapshot(): TariffCheckpoint {
        return TariffCheckpoint(
            code = code,
            label = label,
            tags = tags,
            quantity = quantity,
            pinned = pinned,
            attributes = attributes,
            durationMinutes = durationMinutes,
            maximum = maximum,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tariff checkpoint in status idle. */
fun newTariffCheckpoint(code: String, label: String): TariffCheckpoint {
    return TariffCheckpoint(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        quantity = 0,
        pinned = false,
        attributes = mutableMapOf<String, String>(),
        durationMinutes = 0,
        maximum = 0.0,
        country = "",
        status = TariffCheckpointStatus.IDLE,
    )
}
