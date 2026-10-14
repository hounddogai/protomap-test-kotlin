package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy lane plan bundle is in its life cycle. */
enum class LegacyLanePlanBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy lane plan bundle and its current status. */
data class LegacyLanePlanBundle(
    var code: String,
    var title: String,
    var externalRef: String,
    var graceDays: Int,
    var channel: String,
    var retries: Int,
    var lane: String,
    var vehicleId: String,
    var margin: Double,
    var ratio: Double,
    var status: LegacyLanePlanBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLanePlanBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLanePlanBundleStatus): Boolean {
        when (status) {
            LegacyLanePlanBundleStatus.REQUESTED -> return next == LegacyLanePlanBundleStatus.GRANTED || next == LegacyLanePlanBundleStatus.REVOKED
            LegacyLanePlanBundleStatus.GRANTED -> {
                when (next) {
                    LegacyLanePlanBundleStatus.REVOKED, LegacyLanePlanBundleStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyLanePlanBundleStatus.REVOKED -> return next == LegacyLanePlanBundleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyLanePlanBundleStatus) {
        if (!canMoveTo(next)) {
            error("legacy lane plan bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((margin + retries.toDouble()) * 100) / 100

    fun snapshot(): LegacyLanePlanBundle {
        return LegacyLanePlanBundle(
            code = code,
            title = title,
            externalRef = externalRef,
            graceDays = graceDays,
            channel = channel,
            retries = retries,
            lane = lane,
            vehicleId = vehicleId,
            margin = margin,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy lane plan bundle in status requested. */
fun newLegacyLanePlanBundle(code: String, title: String): LegacyLanePlanBundle {
    return LegacyLanePlanBundle(
        code = code,
        title = title,
        externalRef = "",
        graceDays = 0,
        channel = "",
        retries = 0,
        lane = "",
        vehicleId = "",
        margin = 0.0,
        ratio = 0.0,
        status = LegacyLanePlanBundleStatus.REQUESTED,
    )
}
