package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive near miss summary is in its life cycle. */
enum class AdaptiveNearMissSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive near miss summary as the near miss team keeps it. */
data class AdaptiveNearMissSummary(
    var code: String,
    var name: String,
    var loadKg: Double,
    var etaMinutes: Int,
    var checksum: String,
    var tags: MutableList<String>,
    var carrier: String,
    var revision: Int,
    var batchSize: Int,
    var windowDays: Int,
    var reviewer: String,
    var status: AdaptiveNearMissSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNearMissSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNearMissSummaryStatus): Boolean {
        when (status) {
            AdaptiveNearMissSummaryStatus.NEW -> return next == AdaptiveNearMissSummaryStatus.RUNNING
            AdaptiveNearMissSummaryStatus.QUEUED -> return next == AdaptiveNearMissSummaryStatus.DONE || next == AdaptiveNearMissSummaryStatus.FAILED
            AdaptiveNearMissSummaryStatus.RUNNING -> return next == AdaptiveNearMissSummaryStatus.DONE || next == AdaptiveNearMissSummaryStatus.FAILED
            AdaptiveNearMissSummaryStatus.DONE -> {
                when (next) {
                    AdaptiveNearMissSummaryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveNearMissSummaryStatus) {
        if (!canMoveTo(next)) {
            error("adaptive near miss summary $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"adaptive near miss summary"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round((loadKg + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveNearMissSummary {
        return AdaptiveNearMissSummary(
            code = code,
            name = name,
            loadKg = loadKg,
            etaMinutes = etaMinutes,
            checksum = checksum,
            tags = tags,
            carrier = carrier,
            revision = revision,
            batchSize = batchSize,
            windowDays = windowDays,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive near miss summary in status new. */
fun newAdaptiveNearMissSummary(code: String, name: String): AdaptiveNearMissSummary {
    return AdaptiveNearMissSummary(
        code = code,
        name = name,
        loadKg = 0.0,
        etaMinutes = 0,
        checksum = "",
        tags = mutableListOf<String>(),
        carrier = "",
        revision = 0,
        batchSize = 0,
        windowDays = 0,
        reviewer = "",
        status = AdaptiveNearMissSummaryStatus.NEW,
    )
}
