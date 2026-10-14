package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default service plan manifest is in its life cycle. */
enum class DefaultServicePlanManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default service plan manifest. Its code is unique within a tenant. */
data class DefaultServicePlanManifest(
    var code: String,
    var title: String,
    var archived: Boolean,
    var batchSize: Int,
    var ratio: Double,
    var weight: Double,
    var revision: Int,
    var amount: Double,
    var visible: Boolean,
    var label: String,
    var segment: String,
    var status: DefaultServicePlanManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultServicePlanManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultServicePlanManifestStatus): Boolean {
        when (status) {
            DefaultServicePlanManifestStatus.OPEN -> return next == DefaultServicePlanManifestStatus.RESOLVED || next == DefaultServicePlanManifestStatus.ARCHIVED
            DefaultServicePlanManifestStatus.IN_REVIEW -> {
                when (next) {
                    DefaultServicePlanManifestStatus.REOPENED -> return true
                }
                return false
            }
            DefaultServicePlanManifestStatus.RESOLVED -> {
                when (next) {
                    DefaultServicePlanManifestStatus.REOPENED -> return true
                }
                return false
            }
            DefaultServicePlanManifestStatus.REOPENED -> {
                when (next) {
                    DefaultServicePlanManifestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultServicePlanManifestStatus) {
        if (!canMoveTo(next)) {
            error("default service plan manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$title]"

    fun weightedLoad(): Double = round((ratio - revision.toDouble()) * 100) / 100

    fun snapshot(): DefaultServicePlanManifest {
        return DefaultServicePlanManifest(
            code = code,
            title = title,
            archived = archived,
            batchSize = batchSize,
            ratio = ratio,
            weight = weight,
            revision = revision,
            amount = amount,
            visible = visible,
            label = label,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default service plan manifest in status open. */
fun newDefaultServicePlanManifest(code: String, title: String): DefaultServicePlanManifest {
    return DefaultServicePlanManifest(
        code = code,
        title = title,
        archived = false,
        batchSize = 0,
        ratio = 0.0,
        weight = 0.0,
        revision = 0,
        amount = 0.0,
        visible = false,
        label = "",
        segment = "",
        status = DefaultServicePlanManifestStatus.OPEN,
    )
}
