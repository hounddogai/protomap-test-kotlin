package com.acme.transport.linehaul.laneplan.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled lane plan record is in its life cycle. */
enum class ScheduledLanePlanRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled lane plan record as the lane plan team keeps it. */
data class ScheduledLanePlanRecord(
    var code: String,
    var label: String,
    var contactEmail: String,
    var createdAt: Int,
    var region: String,
    var rate: Double,
    var currency: String,
    var category: String,
    var batchSize: Int,
    var description: String,
    var title: String,
    var status: ScheduledLanePlanRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLanePlanRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLanePlanRecordStatus): Boolean {
        when (status) {
            ScheduledLanePlanRecordStatus.OPEN -> {
                when (next) {
                    ScheduledLanePlanRecordStatus.IN_REVIEW -> return true
                }
                return false
            }
            ScheduledLanePlanRecordStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledLanePlanRecordStatus.RESOLVED, ScheduledLanePlanRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledLanePlanRecordStatus.RESOLVED -> {
                when (next) {
                    ScheduledLanePlanRecordStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledLanePlanRecordStatus.REOPENED -> return next == ScheduledLanePlanRecordStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledLanePlanRecordStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lane plan record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"scheduled lane plan record"} $code"

    fun projectedLoad(): Double = round((rate - createdAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledLanePlanRecord {
        return ScheduledLanePlanRecord(
            code = code,
            label = label,
            contactEmail = contactEmail,
            createdAt = createdAt,
            region = region,
            rate = rate,
            currency = currency,
            category = category,
            batchSize = batchSize,
            description = description,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled lane plan record in status open. */
fun newScheduledLanePlanRecord(code: String, label: String): ScheduledLanePlanRecord {
    return ScheduledLanePlanRecord(
        code = code,
        label = label,
        contactEmail = "",
        createdAt = 0,
        region = "",
        rate = 0.0,
        currency = "",
        category = "",
        batchSize = 0,
        description = "",
        title = "",
        status = ScheduledLanePlanRecordStatus.OPEN,
    )
}
