package com.acme.transport.emissions.evcharge.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default ev charge group is in its life cycle. */
enum class DefaultEvChargeGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default ev charge group. Its code is unique within a tenant. */
data class DefaultEvChargeGroup(
    var code: String,
    var title: String,
    var limit: Int,
    var ceiling: Double,
    var category: String,
    var driverId: String,
    var loadKg: Double,
    var status: DefaultEvChargeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultEvChargeGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultEvChargeGroupStatus): Boolean {
        when (status) {
            DefaultEvChargeGroupStatus.CREATED -> return next == DefaultEvChargeGroupStatus.PUBLISHED || next == DefaultEvChargeGroupStatus.RETIRED
            DefaultEvChargeGroupStatus.VALIDATED -> {
                when (next) {
                    DefaultEvChargeGroupStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultEvChargeGroupStatus.PUBLISHED -> return next == DefaultEvChargeGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultEvChargeGroupStatus) {
        if (!canMoveTo(next)) {
            error("default ev charge group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default ev charge group"} $code"

    fun effectiveExposure(): Double = round((ceiling + limit.toDouble()) * 100) / 100

    fun snapshot(): DefaultEvChargeGroup {
        return DefaultEvChargeGroup(
            code = code,
            title = title,
            limit = limit,
            ceiling = ceiling,
            category = category,
            driverId = driverId,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default ev charge group in status created. */
fun newDefaultEvChargeGroup(code: String, title: String): DefaultEvChargeGroup {
    return DefaultEvChargeGroup(
        code = code,
        title = title,
        limit = 0,
        ceiling = 0.0,
        category = "",
        driverId = "",
        loadKg = 0.0,
        status = DefaultEvChargeGroupStatus.CREATED,
    )
}
