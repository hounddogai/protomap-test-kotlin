package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional fuel burn notice records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a regional fuel burn notice is in its life cycle. */
enum class RegionalFuelBurnNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One regional fuel burn notice, with what the planners need to act on it. */
data class RegionalFuelBurnNotice(
    var code: String,
    var name: String,
    var shiftCode: String,
    var batchSize: Int,
    var label: String,
    var trailerId: String,
    var region: String,
    var ownerId: String,
    var expiresAt: Int,
    var startsAt: Int,
    var contactEmail: String,
    var status: RegionalFuelBurnNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelBurnNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelBurnNoticeStatus): Boolean {
        when (status) {
            RegionalFuelBurnNoticeStatus.IDLE -> {
                when (next) {
                    RegionalFuelBurnNoticeStatus.DRAINING, RegionalFuelBurnNoticeStatus.STOPPED -> return true
                }
                return false
            }
            RegionalFuelBurnNoticeStatus.WARMING -> {
                when (next) {
                    RegionalFuelBurnNoticeStatus.READY, RegionalFuelBurnNoticeStatus.DRAINING -> return true
                }
                return false
            }
            RegionalFuelBurnNoticeStatus.READY -> {
                when (next) {
                    RegionalFuelBurnNoticeStatus.DRAINING, RegionalFuelBurnNoticeStatus.STOPPED -> return true
                }
                return false
            }
            RegionalFuelBurnNoticeStatus.DRAINING -> {
                when (next) {
                    RegionalFuelBurnNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalFuelBurnNoticeStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel burn notice $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun weightedLoad(): Double = round(batchSize.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): RegionalFuelBurnNotice {
        return RegionalFuelBurnNotice(
            code = code,
            name = name,
            shiftCode = shiftCode,
            batchSize = batchSize,
            label = label,
            trailerId = trailerId,
            region = region,
            ownerId = ownerId,
            expiresAt = expiresAt,
            startsAt = startsAt,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a regional fuel burn notice in status idle. */
fun newRegionalFuelBurnNotice(code: String, name: String): RegionalFuelBurnNotice {
    return RegionalFuelBurnNotice(
        code = code,
        name = name,
        shiftCode = "",
        batchSize = 0,
        label = "",
        trailerId = "",
        region = "",
        ownerId = "",
        expiresAt = 0,
        startsAt = 0,
        contactEmail = "",
        status = RegionalFuelBurnNoticeStatus.IDLE,
    )
}
