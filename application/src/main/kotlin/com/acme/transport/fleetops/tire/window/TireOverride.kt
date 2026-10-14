package com.acme.transport.fleetops.tire.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire override is in its life cycle. */
enum class TireOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tire override. Its code is unique within a tenant. */
data class TireOverride(
    var code: String,
    var label: String,
    var contactEmail: String,
    var dock: String,
    var revision: Int,
    var stopCount: Int,
    var region: String,
    var status: TireOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireOverrideStatus): Boolean {
        when (status) {
            TireOverrideStatus.CREATED -> return next == TireOverrideStatus.VALIDATED || next == TireOverrideStatus.PUBLISHED
            TireOverrideStatus.VALIDATED -> return next == TireOverrideStatus.PUBLISHED || next == TireOverrideStatus.RETIRED
            TireOverrideStatus.PUBLISHED -> {
                when (next) {
                    TireOverrideStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireOverrideStatus) {
        if (!canMoveTo(next)) {
            error("tire override $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedLoad(): Double = round((revision.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): TireOverride {
        return TireOverride(
            code = code,
            label = label,
            contactEmail = contactEmail,
            dock = dock,
            revision = revision,
            stopCount = stopCount,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tire override in status created. */
fun newTireOverride(code: String, label: String): TireOverride {
    return TireOverride(
        code = code,
        label = label,
        contactEmail = "",
        dock = "",
        revision = 0,
        stopCount = 0,
        region = "",
        status = TireOverrideStatus.CREATED,
    )
}
