package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global emission factor summary records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a global emission factor summary is in its life cycle. */
enum class GlobalEmissionFactorSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a global emission factor summary and its current status. */
data class GlobalEmissionFactorSummary(
    var code: String,
    var label: String,
    var revision: Int,
    var windowDays: Int,
    var archived: Boolean,
    var rate: Double,
    var status: GlobalEmissionFactorSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalEmissionFactorSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalEmissionFactorSummaryStatus): Boolean {
        when (status) {
            GlobalEmissionFactorSummaryStatus.REQUESTED -> {
                when (next) {
                    GlobalEmissionFactorSummaryStatus.GRANTED, GlobalEmissionFactorSummaryStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalEmissionFactorSummaryStatus.GRANTED -> {
                when (next) {
                    GlobalEmissionFactorSummaryStatus.REVOKED, GlobalEmissionFactorSummaryStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalEmissionFactorSummaryStatus.REVOKED -> {
                when (next) {
                    GlobalEmissionFactorSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalEmissionFactorSummaryStatus) {
        if (!canMoveTo(next)) {
            error("global emission factor summary $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"global emission factor summary"} $code"

    fun projectedTotal(): Double = round(rate * windowDays.toDouble() * 100) / 100

    fun snapshot(): GlobalEmissionFactorSummary {
        return GlobalEmissionFactorSummary(
            code = code,
            label = label,
            revision = revision,
            windowDays = windowDays,
            archived = archived,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global emission factor summary in status requested. */
fun newGlobalEmissionFactorSummary(code: String, label: String): GlobalEmissionFactorSummary {
    return GlobalEmissionFactorSummary(
        code = code,
        label = label,
        revision = 0,
        windowDays = 0,
        archived = false,
        rate = 0.0,
        status = GlobalEmissionFactorSummaryStatus.REQUESTED,
    )
}
