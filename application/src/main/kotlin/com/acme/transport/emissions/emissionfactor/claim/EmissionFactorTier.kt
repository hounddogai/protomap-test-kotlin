package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor tier is in its life cycle. */
enum class EmissionFactorTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A emission factor tier as the emission factor team keeps it. */
data class EmissionFactorTier(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var shiftCode: String,
    var ownerId: String,
    var vehicleId: String,
    var createdAt: Int,
    var visible: Boolean,
    var attributes: MutableMap<String, String>,
    var status: EmissionFactorTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorTierStatus): Boolean {
        when (status) {
            EmissionFactorTierStatus.NEW -> {
                when (next) {
                    EmissionFactorTierStatus.RUNNING, EmissionFactorTierStatus.DONE -> return true
                }
                return false
            }
            EmissionFactorTierStatus.QUEUED -> return next == EmissionFactorTierStatus.RUNNING || next == EmissionFactorTierStatus.DONE
            EmissionFactorTierStatus.RUNNING -> return next == EmissionFactorTierStatus.DONE
            EmissionFactorTierStatus.DONE -> {
                when (next) {
                    EmissionFactorTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorTierStatus) {
        if (!canMoveTo(next)) {
            error("emission factor tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"emission factor tier"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((fuelLiters + createdAt.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorTier {
        return EmissionFactorTier(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            shiftCode = shiftCode,
            ownerId = ownerId,
            vehicleId = vehicleId,
            createdAt = createdAt,
            visible = visible,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a emission factor tier in status new. */
fun newEmissionFactorTier(code: String, title: String): EmissionFactorTier {
    return EmissionFactorTier(
        code = code,
        title = title,
        fuelLiters = 0.0,
        shiftCode = "",
        ownerId = "",
        vehicleId = "",
        createdAt = 0,
        visible = false,
        attributes = mutableMapOf<String, String>(),
        status = EmissionFactorTierStatus.NEW,
    )
}
