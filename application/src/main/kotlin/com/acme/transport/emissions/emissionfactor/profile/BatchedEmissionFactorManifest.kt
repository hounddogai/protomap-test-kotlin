package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched emission factor manifest is in its life cycle. */
enum class BatchedEmissionFactorManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched emission factor manifest. Its code is unique within a tenant. */
data class BatchedEmissionFactorManifest(
    var code: String,
    var label: String,
    var depot: String,
    var attributes: MutableMap<String, String>,
    var minimum: Double,
    var tags: MutableList<String>,
    var expiresAt: Int,
    var status: BatchedEmissionFactorManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedEmissionFactorManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedEmissionFactorManifestStatus): Boolean {
        when (status) {
            BatchedEmissionFactorManifestStatus.PROPOSED -> return next == BatchedEmissionFactorManifestStatus.APPROVED || next == BatchedEmissionFactorManifestStatus.REJECTED
            BatchedEmissionFactorManifestStatus.APPROVED -> {
                when (next) {
                    BatchedEmissionFactorManifestStatus.REJECTED -> return true
                }
                return false
            }
            BatchedEmissionFactorManifestStatus.REJECTED -> return next == BatchedEmissionFactorManifestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedEmissionFactorManifestStatus) {
        if (!canMoveTo(next)) {
            error("batched emission factor manifest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

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

    fun effectiveExposure(): Double = round((minimum + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedEmissionFactorManifest {
        return BatchedEmissionFactorManifest(
            code = code,
            label = label,
            depot = depot,
            attributes = attributes,
            minimum = minimum,
            tags = tags,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched emission factor manifest in status proposed. */
fun newBatchedEmissionFactorManifest(code: String, label: String): BatchedEmissionFactorManifest {
    return BatchedEmissionFactorManifest(
        code = code,
        label = label,
        depot = "",
        attributes = mutableMapOf<String, String>(),
        minimum = 0.0,
        tags = mutableListOf<String>(),
        expiresAt = 0,
        status = BatchedEmissionFactorManifestStatus.PROPOSED,
    )
}
