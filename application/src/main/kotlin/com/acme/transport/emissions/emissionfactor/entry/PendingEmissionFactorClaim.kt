package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending emission factor claim is in its life cycle. */
enum class PendingEmissionFactorClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending emission factor claim. Its code is unique within a tenant. */
data class PendingEmissionFactorClaim(
    var code: String,
    var label: String,
    var batchSize: Int,
    var durationMinutes: Int,
    var attributes: MutableMap<String, String>,
    var active: Boolean,
    var visible: Boolean,
    var tags: MutableList<String>,
    var etaMinutes: Int,
    var stopCount: Int,
    var unit: String,
    var status: PendingEmissionFactorClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingEmissionFactorClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingEmissionFactorClaimStatus): Boolean {
        when (status) {
            PendingEmissionFactorClaimStatus.REQUESTED -> {
                when (next) {
                    PendingEmissionFactorClaimStatus.GRANTED -> return true
                }
                return false
            }
            PendingEmissionFactorClaimStatus.GRANTED -> {
                when (next) {
                    PendingEmissionFactorClaimStatus.REVOKED, PendingEmissionFactorClaimStatus.EXPIRED -> return true
                }
                return false
            }
            PendingEmissionFactorClaimStatus.REVOKED -> {
                when (next) {
                    PendingEmissionFactorClaimStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingEmissionFactorClaimStatus) {
        if (!canMoveTo(next)) {
            error("pending emission factor claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

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

    fun effectiveValue(): Double = round((batchSize.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): PendingEmissionFactorClaim {
        return PendingEmissionFactorClaim(
            code = code,
            label = label,
            batchSize = batchSize,
            durationMinutes = durationMinutes,
            attributes = attributes,
            active = active,
            visible = visible,
            tags = tags,
            etaMinutes = etaMinutes,
            stopCount = stopCount,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending emission factor claim in status requested. */
fun newPendingEmissionFactorClaim(code: String, label: String): PendingEmissionFactorClaim {
    return PendingEmissionFactorClaim(
        code = code,
        label = label,
        batchSize = 0,
        durationMinutes = 0,
        attributes = mutableMapOf<String, String>(),
        active = false,
        visible = false,
        tags = mutableListOf<String>(),
        etaMinutes = 0,
        stopCount = 0,
        unit = "",
        status = PendingEmissionFactorClaimStatus.REQUESTED,
    )
}
