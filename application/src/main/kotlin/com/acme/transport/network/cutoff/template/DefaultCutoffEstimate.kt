package com.acme.transport.network.cutoff.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default cutoff estimate is in its life cycle. */
enum class DefaultCutoffEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default cutoff estimate as the cutoff team keeps it. */
data class DefaultCutoffEstimate(
    var code: String,
    var title: String,
    var floorValue: Double,
    var country: String,
    var budget: Double,
    var active: Boolean,
    var segment: String,
    var status: DefaultCutoffEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCutoffEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCutoffEstimateStatus): Boolean {
        when (status) {
            DefaultCutoffEstimateStatus.CREATED -> return next == DefaultCutoffEstimateStatus.VALIDATED || next == DefaultCutoffEstimateStatus.PUBLISHED
            DefaultCutoffEstimateStatus.VALIDATED -> {
                when (next) {
                    DefaultCutoffEstimateStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultCutoffEstimateStatus.PUBLISHED -> return next == DefaultCutoffEstimateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultCutoffEstimateStatus) {
        if (!canMoveTo(next)) {
            error("default cutoff estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default cutoff estimate"} $code"

    fun effectiveLoad(): Double = round((budget - floorValue) * 100) / 100

    fun snapshot(): DefaultCutoffEstimate {
        return DefaultCutoffEstimate(
            code = code,
            title = title,
            floorValue = floorValue,
            country = country,
            budget = budget,
            active = active,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a default cutoff estimate in status created. */
fun newDefaultCutoffEstimate(code: String, title: String): DefaultCutoffEstimate {
    return DefaultCutoffEstimate(
        code = code,
        title = title,
        floorValue = 0.0,
        country = "",
        budget = 0.0,
        active = false,
        segment = "",
        status = DefaultCutoffEstimateStatus.CREATED,
    )
}
