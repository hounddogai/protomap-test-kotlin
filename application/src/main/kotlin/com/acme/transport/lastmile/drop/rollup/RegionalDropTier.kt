package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional drop tier is in its life cycle. */
enum class RegionalDropTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a regional drop tier and its current status. */
data class RegionalDropTier(
    var code: String,
    var title: String,
    var contactEmail: String,
    var locale: String,
    var channel: String,
    var batchSize: Int,
    var pallets: Int,
    var ownerId: String,
    var expiresAt: Int,
    var etaMinutes: Int,
    var routeCode: String,
    var status: RegionalDropTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDropTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDropTierStatus): Boolean {
        when (status) {
            RegionalDropTierStatus.OPEN -> return next == RegionalDropTierStatus.REOPENED
            RegionalDropTierStatus.IN_REVIEW -> return next == RegionalDropTierStatus.RESOLVED || next == RegionalDropTierStatus.REOPENED
            RegionalDropTierStatus.RESOLVED -> {
                when (next) {
                    RegionalDropTierStatus.REOPENED -> return true
                }
                return false
            }
            RegionalDropTierStatus.REOPENED -> {
                when (next) {
                    RegionalDropTierStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDropTierStatus) {
        if (!canMoveTo(next)) {
            error("regional drop tier $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((batchSize.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): RegionalDropTier {
        return RegionalDropTier(
            code = code,
            title = title,
            contactEmail = contactEmail,
            locale = locale,
            channel = channel,
            batchSize = batchSize,
            pallets = pallets,
            ownerId = ownerId,
            expiresAt = expiresAt,
            etaMinutes = etaMinutes,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional drop tier in status open. */
fun newRegionalDropTier(code: String, title: String): RegionalDropTier {
    return RegionalDropTier(
        code = code,
        title = title,
        contactEmail = "",
        locale = "",
        channel = "",
        batchSize = 0,
        pallets = 0,
        ownerId = "",
        expiresAt = 0,
        etaMinutes = 0,
        routeCode = "",
        status = RegionalDropTierStatus.OPEN,
    )
}
