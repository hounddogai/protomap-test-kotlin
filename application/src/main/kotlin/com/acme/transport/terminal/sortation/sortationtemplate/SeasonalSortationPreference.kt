package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal sortation preference is in its life cycle. */
enum class SeasonalSortationPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal sortation preference as the sortation team keeps it. */
data class SeasonalSortationPreference(
    var code: String,
    var name: String,
    var source: String,
    var label: String,
    var distanceKm: Double,
    var durationMinutes: Int,
    var notes: String,
    var odometer: Int,
    var category: String,
    var quantity: Int,
    var status: SeasonalSortationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalSortationPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalSortationPreferenceStatus): Boolean {
        when (status) {
            SeasonalSortationPreferenceStatus.IDLE -> return next == SeasonalSortationPreferenceStatus.READY || next == SeasonalSortationPreferenceStatus.STOPPED
            SeasonalSortationPreferenceStatus.WARMING -> return next == SeasonalSortationPreferenceStatus.STOPPED
            SeasonalSortationPreferenceStatus.READY -> {
                when (next) {
                    SeasonalSortationPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalSortationPreferenceStatus.DRAINING -> {
                when (next) {
                    SeasonalSortationPreferenceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalSortationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("seasonal sortation preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun netTotal(): Double = round((quantity.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): SeasonalSortationPreference {
        return SeasonalSortationPreference(
            code = code,
            name = name,
            source = source,
            label = label,
            distanceKm = distanceKm,
            durationMinutes = durationMinutes,
            notes = notes,
            odometer = odometer,
            category = category,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal sortation preference in status idle. */
fun newSeasonalSortationPreference(code: String, name: String): SeasonalSortationPreference {
    return SeasonalSortationPreference(
        code = code,
        name = name,
        source = "",
        label = "",
        distanceKm = 0.0,
        durationMinutes = 0,
        notes = "",
        odometer = 0,
        category = "",
        quantity = 0,
        status = SeasonalSortationPreferenceStatus.IDLE,
    )
}
