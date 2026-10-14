package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tender tier records of linehaul's tender work, and the rules, stores, and calculations around them.

/** Where a tender tier is in its life cycle. */
enum class TenderTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tender tier and its current status. */
data class TenderTier(
    var code: String,
    var name: String,
    var rate: Double,
    var phone: String,
    var batchSize: Int,
    var timezone: String,
    var lane: String,
    var weight: Double,
    var visible: Boolean,
    var cost: Double,
    var status: TenderTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderTierStatus): Boolean {
        when (status) {
            TenderTierStatus.NEW -> {
                when (next) {
                    TenderTierStatus.RUNNING, TenderTierStatus.DONE -> return true
                }
                return false
            }
            TenderTierStatus.QUEUED -> return next == TenderTierStatus.RUNNING || next == TenderTierStatus.FAILED
            TenderTierStatus.RUNNING -> {
                when (next) {
                    TenderTierStatus.DONE, TenderTierStatus.FAILED -> return true
                }
                return false
            }
            TenderTierStatus.DONE -> {
                when (next) {
                    TenderTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderTierStatus) {
        if (!canMoveTo(next)) {
            error("tender tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedLoad(): Double = round((weight + rate) * 100) / 100

    fun snapshot(): TenderTier {
        return TenderTier(
            code = code,
            name = name,
            rate = rate,
            phone = phone,
            batchSize = batchSize,
            timezone = timezone,
            lane = lane,
            weight = weight,
            visible = visible,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tender tier in status new. */
fun newTenderTier(code: String, name: String): TenderTier {
    return TenderTier(
        code = code,
        name = name,
        rate = 0.0,
        phone = "",
        batchSize = 0,
        timezone = "",
        lane = "",
        weight = 0.0,
        visible = false,
        cost = 0.0,
        status = TenderTierStatus.NEW,
    )
}
