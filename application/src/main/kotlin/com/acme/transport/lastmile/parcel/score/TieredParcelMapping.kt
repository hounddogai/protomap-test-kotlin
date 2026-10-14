package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered parcel mapping is in its life cycle. */
enum class TieredParcelMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered parcel mapping as the parcel team keeps it. */
data class TieredParcelMapping(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var startsAt: Int,
    var durationMinutes: Int,
    var carrier: String,
    var visible: Boolean,
    var createdAt: Int,
    var status: TieredParcelMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredParcelMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredParcelMappingStatus): Boolean {
        when (status) {
            TieredParcelMappingStatus.OPEN -> {
                when (next) {
                    TieredParcelMappingStatus.IN_REVIEW, TieredParcelMappingStatus.REOPENED -> return true
                }
                return false
            }
            TieredParcelMappingStatus.IN_REVIEW -> {
                when (next) {
                    TieredParcelMappingStatus.REOPENED, TieredParcelMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredParcelMappingStatus.RESOLVED -> return next == TieredParcelMappingStatus.REOPENED || next == TieredParcelMappingStatus.ARCHIVED
            TieredParcelMappingStatus.REOPENED -> {
                when (next) {
                    TieredParcelMappingStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredParcelMappingStatus) {
        if (!canMoveTo(next)) {
            error("tiered parcel mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netVolume(): Double = round((durationMinutes.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): TieredParcelMapping {
        return TieredParcelMapping(
            code = code,
            name = name,
            expiresAt = expiresAt,
            startsAt = startsAt,
            durationMinutes = durationMinutes,
            carrier = carrier,
            visible = visible,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered parcel mapping in status open. */
fun newTieredParcelMapping(code: String, name: String): TieredParcelMapping {
    return TieredParcelMapping(
        code = code,
        name = name,
        expiresAt = 0,
        startsAt = 0,
        durationMinutes = 0,
        carrier = "",
        visible = false,
        createdAt = 0,
        status = TieredParcelMappingStatus.OPEN,
    )
}
