package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.text.normalize

/** Where a redelivery manifest is in its life cycle. */
enum class RedeliveryManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a redelivery manifest and its current status. */
data class RedeliveryManifest(
    var code: String,
    var label: String,
    var name: String,
    var fuelLiters: Double,
    var category: String,
    var status: RedeliveryManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryManifestStatus): Boolean {
        when (status) {
            RedeliveryManifestStatus.CREATED -> {
                when (next) {
                    RedeliveryManifestStatus.PUBLISHED -> return true
                }
                return false
            }
            RedeliveryManifestStatus.VALIDATED -> return next == RedeliveryManifestStatus.PUBLISHED
            RedeliveryManifestStatus.PUBLISHED -> {
                when (next) {
                    RedeliveryManifestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryManifestStatus) {
        if (!canMoveTo(next)) {
            error("redelivery manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"redelivery manifest"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): RedeliveryManifest {
        return RedeliveryManifest(
            code = code,
            label = label,
            name = name,
            fuelLiters = fuelLiters,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a redelivery manifest in status created. */
fun newRedeliveryManifest(code: String, label: String): RedeliveryManifest {
    return RedeliveryManifest(
        code = code,
        label = label,
        name = "",
        fuelLiters = 0.0,
        category = "",
        status = RedeliveryManifestStatus.CREATED,
    )
}
