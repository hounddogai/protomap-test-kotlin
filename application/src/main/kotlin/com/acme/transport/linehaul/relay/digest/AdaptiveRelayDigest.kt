package com.acme.transport.linehaul.relay.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive relay digest records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a adaptive relay digest is in its life cycle. */
enum class AdaptiveRelayDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive relay digest. Its code is unique within a tenant. */
data class AdaptiveRelayDigest(
    var code: String,
    var label: String,
    var batchSize: Int,
    var name: String,
    var region: String,
    var windowDays: Int,
    var summary: String,
    var phone: String,
    var source: String,
    var expiresAt: Int,
    var timezone: String,
    var status: AdaptiveRelayDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRelayDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRelayDigestStatus): Boolean {
        when (status) {
            AdaptiveRelayDigestStatus.NEW -> return next == AdaptiveRelayDigestStatus.QUEUED || next == AdaptiveRelayDigestStatus.FAILED
            AdaptiveRelayDigestStatus.QUEUED -> {
                when (next) {
                    AdaptiveRelayDigestStatus.DONE, AdaptiveRelayDigestStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveRelayDigestStatus.RUNNING -> return next == AdaptiveRelayDigestStatus.FAILED
            AdaptiveRelayDigestStatus.DONE -> return next == AdaptiveRelayDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveRelayDigestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive relay digest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netExposure(): Double = round(expiresAt.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): AdaptiveRelayDigest {
        return AdaptiveRelayDigest(
            code = code,
            label = label,
            batchSize = batchSize,
            name = name,
            region = region,
            windowDays = windowDays,
            summary = summary,
            phone = phone,
            source = source,
            expiresAt = expiresAt,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive relay digest in status new. */
fun newAdaptiveRelayDigest(code: String, label: String): AdaptiveRelayDigest {
    return AdaptiveRelayDigest(
        code = code,
        label = label,
        batchSize = 0,
        name = "",
        region = "",
        windowDays = 0,
        summary = "",
        phone = "",
        source = "",
        expiresAt = 0,
        timezone = "",
        status = AdaptiveRelayDigestStatus.NEW,
    )
}
