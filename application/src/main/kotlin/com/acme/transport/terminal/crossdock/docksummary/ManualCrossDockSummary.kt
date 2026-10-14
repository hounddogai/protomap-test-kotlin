package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual cross dock summary records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a manual cross dock summary is in its life cycle. */
enum class ManualCrossDockSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual cross dock summary as the cross dock team keeps it. */
data class ManualCrossDockSummary(
    var code: String,
    var name: String,
    var revision: Int,
    var weight: Double,
    var retries: Int,
    var timezone: String,
    var quantity: Int,
    var createdAt: Int,
    var startsAt: Int,
    var externalRef: String,
    var status: ManualCrossDockSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCrossDockSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCrossDockSummaryStatus): Boolean {
        when (status) {
            ManualCrossDockSummaryStatus.IDLE -> return next == ManualCrossDockSummaryStatus.WARMING || next == ManualCrossDockSummaryStatus.READY
            ManualCrossDockSummaryStatus.WARMING -> {
                when (next) {
                    ManualCrossDockSummaryStatus.READY, ManualCrossDockSummaryStatus.STOPPED -> return true
                }
                return false
            }
            ManualCrossDockSummaryStatus.READY -> {
                when (next) {
                    ManualCrossDockSummaryStatus.DRAINING, ManualCrossDockSummaryStatus.STOPPED -> return true
                }
                return false
            }
            ManualCrossDockSummaryStatus.DRAINING -> {
                when (next) {
                    ManualCrossDockSummaryStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCrossDockSummaryStatus) {
        if (!canMoveTo(next)) {
            error("manual cross dock summary $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round((createdAt.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): ManualCrossDockSummary {
        return ManualCrossDockSummary(
            code = code,
            name = name,
            revision = revision,
            weight = weight,
            retries = retries,
            timezone = timezone,
            quantity = quantity,
            createdAt = createdAt,
            startsAt = startsAt,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual cross dock summary in status idle. */
fun newManualCrossDockSummary(code: String, name: String): ManualCrossDockSummary {
    return ManualCrossDockSummary(
        code = code,
        name = name,
        revision = 0,
        weight = 0.0,
        retries = 0,
        timezone = "",
        quantity = 0,
        createdAt = 0,
        startsAt = 0,
        externalRef = "",
        status = ManualCrossDockSummaryStatus.IDLE,
    )
}
