package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary fuel burn notice is in its life cycle. */
enum class PrimaryFuelBurnNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a primary fuel burn notice and its current status. */
data class PrimaryFuelBurnNotice(
    var code: String,
    var name: String,
    var createdAt: Int,
    var approved: Boolean,
    var floorValue: Double,
    var pallets: Int,
    var durationMinutes: Int,
    var visible: Boolean,
    var shiftCode: String,
    var tags: MutableList<String>,
    var updatedAt: Int,
    var status: PrimaryFuelBurnNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFuelBurnNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFuelBurnNoticeStatus): Boolean {
        when (status) {
            PrimaryFuelBurnNoticeStatus.PROPOSED -> {
                when (next) {
                    PrimaryFuelBurnNoticeStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryFuelBurnNoticeStatus.APPROVED -> return next == PrimaryFuelBurnNoticeStatus.REJECTED
            PrimaryFuelBurnNoticeStatus.REJECTED -> return next == PrimaryFuelBurnNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryFuelBurnNoticeStatus) {
        if (!canMoveTo(next)) {
            error("primary fuel burn notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round((createdAt.toDouble() + floorValue) * 100) / 100

    fun snapshot(): PrimaryFuelBurnNotice {
        return PrimaryFuelBurnNotice(
            code = code,
            name = name,
            createdAt = createdAt,
            approved = approved,
            floorValue = floorValue,
            pallets = pallets,
            durationMinutes = durationMinutes,
            visible = visible,
            shiftCode = shiftCode,
            tags = tags,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary fuel burn notice in status proposed. */
fun newPrimaryFuelBurnNotice(code: String, name: String): PrimaryFuelBurnNotice {
    return PrimaryFuelBurnNotice(
        code = code,
        name = name,
        createdAt = 0,
        approved = false,
        floorValue = 0.0,
        pallets = 0,
        durationMinutes = 0,
        visible = false,
        shiftCode = "",
        tags = mutableListOf<String>(),
        updatedAt = 0,
        status = PrimaryFuelBurnNoticeStatus.PROPOSED,
    )
}
