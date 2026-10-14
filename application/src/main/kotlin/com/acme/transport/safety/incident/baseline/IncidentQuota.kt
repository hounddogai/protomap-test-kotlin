package com.acme.transport.safety.incident.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident quota is in its life cycle. */
enum class IncidentQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A incident quota. Its code is unique within a tenant. */
data class IncidentQuota(
    var code: String,
    var name: String,
    var revision: Int,
    var phone: String,
    var lane: String,
    var label: String,
    var batchSize: Int,
    var threshold: Double,
    var status: IncidentQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentQuotaStatus): Boolean {
        when (status) {
            IncidentQuotaStatus.CREATED -> {
                when (next) {
                    IncidentQuotaStatus.VALIDATED, IncidentQuotaStatus.PUBLISHED -> return true
                }
                return false
            }
            IncidentQuotaStatus.VALIDATED -> {
                when (next) {
                    IncidentQuotaStatus.RETIRED -> return true
                }
                return false
            }
            IncidentQuotaStatus.PUBLISHED -> return next == IncidentQuotaStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: IncidentQuotaStatus) {
        if (!canMoveTo(next)) {
            error("incident quota $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"incident quota"} $code"

    fun weightedExposure(): Double = round((batchSize.toDouble() + threshold) * 100) / 100

    fun snapshot(): IncidentQuota {
        return IncidentQuota(
            code = code,
            name = name,
            revision = revision,
            phone = phone,
            lane = lane,
            label = label,
            batchSize = batchSize,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a incident quota in status created. */
fun newIncidentQuota(code: String, name: String): IncidentQuota {
    return IncidentQuota(
        code = code,
        name = name,
        revision = 0,
        phone = "",
        lane = "",
        label = "",
        batchSize = 0,
        threshold = 0.0,
        status = IncidentQuotaStatus.CREATED,
    )
}
