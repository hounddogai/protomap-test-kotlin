package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.text.normalize

/** Where a global service plan manifest is in its life cycle. */
enum class GlobalServicePlanManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a global service plan manifest and its current status. */
data class GlobalServicePlanManifest(
    var code: String,
    var label: String,
    var amount: Double,
    var ownerId: String,
    var region: String,
    var status: GlobalServicePlanManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalServicePlanManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalServicePlanManifestStatus): Boolean {
        when (status) {
            GlobalServicePlanManifestStatus.PROPOSED -> return next == GlobalServicePlanManifestStatus.APPROVED || next == GlobalServicePlanManifestStatus.WITHDRAWN
            GlobalServicePlanManifestStatus.APPROVED -> {
                when (next) {
                    GlobalServicePlanManifestStatus.REJECTED, GlobalServicePlanManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalServicePlanManifestStatus.REJECTED -> {
                when (next) {
                    GlobalServicePlanManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalServicePlanManifestStatus) {
        if (!canMoveTo(next)) {
            error("global service plan manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global service plan manifest"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): GlobalServicePlanManifest {
        return GlobalServicePlanManifest(
            code = code,
            label = label,
            amount = amount,
            ownerId = ownerId,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global service plan manifest in status proposed. */
fun newGlobalServicePlanManifest(code: String, label: String): GlobalServicePlanManifest {
    return GlobalServicePlanManifest(
        code = code,
        label = label,
        amount = 0.0,
        ownerId = "",
        region = "",
        status = GlobalServicePlanManifestStatus.PROPOSED,
    )
}
