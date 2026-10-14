package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted backhaul threshold is in its life cycle. */
enum class WeightedBackhaulThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted backhaul threshold. Its code is unique within a tenant. */
data class WeightedBackhaulThreshold(
    var code: String,
    var name: String,
    var version: Int,
    var country: String,
    var shiftCode: String,
    var summary: String,
    var approved: Boolean,
    var updatedAt: Int,
    var archived: Boolean,
    var windowDays: Int,
    var revision: Int,
    var status: WeightedBackhaulThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedBackhaulThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedBackhaulThresholdStatus): Boolean {
        when (status) {
            WeightedBackhaulThresholdStatus.CREATED -> {
                when (next) {
                    WeightedBackhaulThresholdStatus.PUBLISHED -> return true
                }
                return false
            }
            WeightedBackhaulThresholdStatus.VALIDATED -> return next == WeightedBackhaulThresholdStatus.PUBLISHED || next == WeightedBackhaulThresholdStatus.RETIRED
            WeightedBackhaulThresholdStatus.PUBLISHED -> {
                when (next) {
                    WeightedBackhaulThresholdStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedBackhaulThresholdStatus) {
        if (!canMoveTo(next)) {
            error("weighted backhaul threshold $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round(revision.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): WeightedBackhaulThreshold {
        return WeightedBackhaulThreshold(
            code = code,
            name = name,
            version = version,
            country = country,
            shiftCode = shiftCode,
            summary = summary,
            approved = approved,
            updatedAt = updatedAt,
            archived = archived,
            windowDays = windowDays,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a weighted backhaul threshold in status created. */
fun newWeightedBackhaulThreshold(code: String, name: String): WeightedBackhaulThreshold {
    return WeightedBackhaulThreshold(
        code = code,
        name = name,
        version = 0,
        country = "",
        shiftCode = "",
        summary = "",
        approved = false,
        updatedAt = 0,
        archived = false,
        windowDays = 0,
        revision = 0,
        status = WeightedBackhaulThresholdStatus.CREATED,
    )
}
