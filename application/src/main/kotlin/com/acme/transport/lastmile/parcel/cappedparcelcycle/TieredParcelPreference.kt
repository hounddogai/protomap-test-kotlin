package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered parcel preference is in its life cycle. */
enum class TieredParcelPreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered parcel preference as the parcel team keeps it. */
data class TieredParcelPreference(
    var code: String,
    var title: String,
    var score: Double,
    var active: Boolean,
    var endsAt: Int,
    var floorValue: Double,
    var description: String,
    var startsAt: Int,
    var status: TieredParcelPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredParcelPreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredParcelPreferenceStatus): Boolean {
        when (status) {
            TieredParcelPreferenceStatus.CREATED -> return next == TieredParcelPreferenceStatus.PUBLISHED || next == TieredParcelPreferenceStatus.RETIRED
            TieredParcelPreferenceStatus.VALIDATED -> return next == TieredParcelPreferenceStatus.PUBLISHED || next == TieredParcelPreferenceStatus.RETIRED
            TieredParcelPreferenceStatus.PUBLISHED -> return next == TieredParcelPreferenceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredParcelPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("tiered parcel preference $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round(floorValue * startsAt.toDouble() * 100) / 100

    fun snapshot(): TieredParcelPreference {
        return TieredParcelPreference(
            code = code,
            title = title,
            score = score,
            active = active,
            endsAt = endsAt,
            floorValue = floorValue,
            description = description,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered parcel preference in status created. */
fun newTieredParcelPreference(code: String, title: String): TieredParcelPreference {
    return TieredParcelPreference(
        code = code,
        title = title,
        score = 0.0,
        active = false,
        endsAt = 0,
        floorValue = 0.0,
        description = "",
        startsAt = 0,
        status = TieredParcelPreferenceStatus.CREATED,
    )
}
