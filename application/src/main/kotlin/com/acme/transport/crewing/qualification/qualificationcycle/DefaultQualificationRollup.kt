package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default qualification rollup is in its life cycle. */
enum class DefaultQualificationRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default qualification rollup as the qualification team keeps it. */
data class DefaultQualificationRollup(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var description: String,
    var summary: String,
    var floorValue: Double,
    var stopCount: Int,
    var externalRef: String,
    var loadKg: Double,
    var status: DefaultQualificationRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultQualificationRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultQualificationRollupStatus): Boolean {
        when (status) {
            DefaultQualificationRollupStatus.CREATED -> return next == DefaultQualificationRollupStatus.VALIDATED
            DefaultQualificationRollupStatus.VALIDATED -> {
                when (next) {
                    DefaultQualificationRollupStatus.PUBLISHED, DefaultQualificationRollupStatus.RETIRED -> return true
                }
                return false
            }
            DefaultQualificationRollupStatus.PUBLISHED -> return next == DefaultQualificationRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultQualificationRollupStatus) {
        if (!canMoveTo(next)) {
            error("default qualification rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default qualification rollup"} $code"

    fun projectedValue(): Double = round((loadKg + floorValue) * 100) / 100

    fun snapshot(): DefaultQualificationRollup {
        return DefaultQualificationRollup(
            code = code,
            label = label,
            pinned = pinned,
            description = description,
            summary = summary,
            floorValue = floorValue,
            stopCount = stopCount,
            externalRef = externalRef,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default qualification rollup in status created. */
fun newDefaultQualificationRollup(code: String, label: String): DefaultQualificationRollup {
    return DefaultQualificationRollup(
        code = code,
        label = label,
        pinned = false,
        description = "",
        summary = "",
        floorValue = 0.0,
        stopCount = 0,
        externalRef = "",
        loadKg = 0.0,
        status = DefaultQualificationRollupStatus.CREATED,
    )
}
