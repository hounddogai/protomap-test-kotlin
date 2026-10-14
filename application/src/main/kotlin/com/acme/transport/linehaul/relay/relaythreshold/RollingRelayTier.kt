package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling relay tier is in its life cycle. */
enum class RollingRelayTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling relay tier. Its code is unique within a tenant. */
data class RollingRelayTier(
    var code: String,
    var title: String,
    var phone: String,
    var lane: String,
    var pallets: Int,
    var retries: Int,
    var description: String,
    var budget: Double,
    var region: String,
    var status: RollingRelayTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRelayTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRelayTierStatus): Boolean {
        when (status) {
            RollingRelayTierStatus.OPEN -> return next == RollingRelayTierStatus.IN_REVIEW || next == RollingRelayTierStatus.ARCHIVED
            RollingRelayTierStatus.IN_REVIEW -> return next == RollingRelayTierStatus.RESOLVED || next == RollingRelayTierStatus.REOPENED
            RollingRelayTierStatus.RESOLVED -> {
                when (next) {
                    RollingRelayTierStatus.REOPENED, RollingRelayTierStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingRelayTierStatus.REOPENED -> {
                when (next) {
                    RollingRelayTierStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRelayTierStatus) {
        if (!canMoveTo(next)) {
            error("rolling relay tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedLoad(): Double = round(budget * pallets.toDouble() * 100) / 100

    fun snapshot(): RollingRelayTier {
        return RollingRelayTier(
            code = code,
            title = title,
            phone = phone,
            lane = lane,
            pallets = pallets,
            retries = retries,
            description = description,
            budget = budget,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling relay tier in status open. */
fun newRollingRelayTier(code: String, title: String): RollingRelayTier {
    return RollingRelayTier(
        code = code,
        title = title,
        phone = "",
        lane = "",
        pallets = 0,
        retries = 0,
        description = "",
        budget = 0.0,
        region = "",
        status = RollingRelayTierStatus.OPEN,
    )
}
