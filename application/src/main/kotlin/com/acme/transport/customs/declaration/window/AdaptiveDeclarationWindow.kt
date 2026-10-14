package com.acme.transport.customs.declaration.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive declaration window records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a adaptive declaration window is in its life cycle. */
enum class AdaptiveDeclarationWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One adaptive declaration window, with what the planners need to act on it. */
data class AdaptiveDeclarationWindow(
    var code: String,
    var label: String,
    var country: String,
    var createdAt: Int,
    var vehicleId: String,
    var region: String,
    var unit: String,
    var category: String,
    var durationMinutes: Int,
    var shiftCode: String,
    var status: AdaptiveDeclarationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDeclarationWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDeclarationWindowStatus): Boolean {
        when (status) {
            AdaptiveDeclarationWindowStatus.CREATED -> return next == AdaptiveDeclarationWindowStatus.VALIDATED || next == AdaptiveDeclarationWindowStatus.RETIRED
            AdaptiveDeclarationWindowStatus.VALIDATED -> {
                when (next) {
                    AdaptiveDeclarationWindowStatus.PUBLISHED, AdaptiveDeclarationWindowStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveDeclarationWindowStatus.PUBLISHED -> return next == AdaptiveDeclarationWindowStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveDeclarationWindowStatus) {
        if (!canMoveTo(next)) {
            error("adaptive declaration window $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round(createdAt.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): AdaptiveDeclarationWindow {
        return AdaptiveDeclarationWindow(
            code = code,
            label = label,
            country = country,
            createdAt = createdAt,
            vehicleId = vehicleId,
            region = region,
            unit = unit,
            category = category,
            durationMinutes = durationMinutes,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive declaration window in status created. */
fun newAdaptiveDeclarationWindow(code: String, label: String): AdaptiveDeclarationWindow {
    return AdaptiveDeclarationWindow(
        code = code,
        label = label,
        country = "",
        createdAt = 0,
        vehicleId = "",
        region = "",
        unit = "",
        category = "",
        durationMinutes = 0,
        shiftCode = "",
        status = AdaptiveDeclarationWindowStatus.CREATED,
    )
}
