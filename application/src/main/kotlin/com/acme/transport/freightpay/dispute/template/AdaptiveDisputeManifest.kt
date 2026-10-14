package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive dispute manifest is in its life cycle. */
enum class AdaptiveDisputeManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One adaptive dispute manifest, with what the planners need to act on it. */
data class AdaptiveDisputeManifest(
    var code: String,
    var name: String,
    var rate: Double,
    var timezone: String,
    var ratio: Double,
    var status: AdaptiveDisputeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDisputeManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDisputeManifestStatus): Boolean {
        when (status) {
            AdaptiveDisputeManifestStatus.NEW -> {
                when (next) {
                    AdaptiveDisputeManifestStatus.QUEUED, AdaptiveDisputeManifestStatus.RUNNING -> return true
                }
                return false
            }
            AdaptiveDisputeManifestStatus.QUEUED -> {
                when (next) {
                    AdaptiveDisputeManifestStatus.RUNNING -> return true
                }
                return false
            }
            AdaptiveDisputeManifestStatus.RUNNING -> return next == AdaptiveDisputeManifestStatus.DONE || next == AdaptiveDisputeManifestStatus.FAILED
            AdaptiveDisputeManifestStatus.DONE -> {
                when (next) {
                    AdaptiveDisputeManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDisputeManifestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive dispute manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive dispute manifest"} $code"

    fun projectedExposure(): Double = round((rate + ratio) * 100) / 100

    fun snapshot(): AdaptiveDisputeManifest {
        return AdaptiveDisputeManifest(
            code = code,
            name = name,
            rate = rate,
            timezone = timezone,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive dispute manifest in status new. */
fun newAdaptiveDisputeManifest(code: String, name: String): AdaptiveDisputeManifest {
    return AdaptiveDisputeManifest(
        code = code,
        name = name,
        rate = 0.0,
        timezone = "",
        ratio = 0.0,
        status = AdaptiveDisputeManifestStatus.NEW,
    )
}
