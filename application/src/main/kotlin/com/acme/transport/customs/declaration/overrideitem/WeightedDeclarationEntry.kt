package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted declaration entry is in its life cycle. */
enum class WeightedDeclarationEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One weighted declaration entry, with what the planners need to act on it. */
data class WeightedDeclarationEntry(
    var code: String,
    var title: String,
    var routeCode: String,
    var expiresAt: Int,
    var fuelLiters: Double,
    var category: String,
    var windowDays: Int,
    var label: String,
    var checksum: String,
    var segment: String,
    var country: String,
    var status: WeightedDeclarationEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDeclarationEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDeclarationEntryStatus): Boolean {
        when (status) {
            WeightedDeclarationEntryStatus.CREATED -> {
                when (next) {
                    WeightedDeclarationEntryStatus.RETIRED -> return true
                }
                return false
            }
            WeightedDeclarationEntryStatus.VALIDATED -> return next == WeightedDeclarationEntryStatus.PUBLISHED
            WeightedDeclarationEntryStatus.PUBLISHED -> {
                when (next) {
                    WeightedDeclarationEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDeclarationEntryStatus) {
        if (!canMoveTo(next)) {
            error("weighted declaration entry $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($title)"

    fun projectedExposure(): Double = round((windowDays.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedDeclarationEntry {
        return WeightedDeclarationEntry(
            code = code,
            title = title,
            routeCode = routeCode,
            expiresAt = expiresAt,
            fuelLiters = fuelLiters,
            category = category,
            windowDays = windowDays,
            label = label,
            checksum = checksum,
            segment = segment,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted declaration entry in status created. */
fun newWeightedDeclarationEntry(code: String, title: String): WeightedDeclarationEntry {
    return WeightedDeclarationEntry(
        code = code,
        title = title,
        routeCode = "",
        expiresAt = 0,
        fuelLiters = 0.0,
        category = "",
        windowDays = 0,
        label = "",
        checksum = "",
        segment = "",
        country = "",
        status = WeightedDeclarationEntryStatus.CREATED,
    )
}
