package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted service plan manifest is in its life cycle. */
enum class WeightedServicePlanManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted service plan manifest. Its code is unique within a tenant. */
data class WeightedServicePlanManifest(
    var code: String,
    var title: String,
    var startsAt: Int,
    var externalRef: String,
    var pallets: Int,
    var active: Boolean,
    var visible: Boolean,
    var approved: Boolean,
    var margin: Double,
    var etaMinutes: Int,
    var reviewer: String,
    var status: WeightedServicePlanManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedServicePlanManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedServicePlanManifestStatus): Boolean {
        when (status) {
            WeightedServicePlanManifestStatus.REQUESTED -> {
                when (next) {
                    WeightedServicePlanManifestStatus.GRANTED, WeightedServicePlanManifestStatus.REVOKED -> return true
                }
                return false
            }
            WeightedServicePlanManifestStatus.GRANTED -> return next == WeightedServicePlanManifestStatus.REVOKED || next == WeightedServicePlanManifestStatus.EXPIRED
            WeightedServicePlanManifestStatus.REVOKED -> {
                when (next) {
                    WeightedServicePlanManifestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedServicePlanManifestStatus) {
        if (!canMoveTo(next)) {
            error("weighted service plan manifest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round((etaMinutes.toDouble() + margin) * 100) / 100

    fun snapshot(): WeightedServicePlanManifest {
        return WeightedServicePlanManifest(
            code = code,
            title = title,
            startsAt = startsAt,
            externalRef = externalRef,
            pallets = pallets,
            active = active,
            visible = visible,
            approved = approved,
            margin = margin,
            etaMinutes = etaMinutes,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted service plan manifest in status requested. */
fun newWeightedServicePlanManifest(code: String, title: String): WeightedServicePlanManifest {
    return WeightedServicePlanManifest(
        code = code,
        title = title,
        startsAt = 0,
        externalRef = "",
        pallets = 0,
        active = false,
        visible = false,
        approved = false,
        margin = 0.0,
        etaMinutes = 0,
        reviewer = "",
        status = WeightedServicePlanManifestStatus.REQUESTED,
    )
}
