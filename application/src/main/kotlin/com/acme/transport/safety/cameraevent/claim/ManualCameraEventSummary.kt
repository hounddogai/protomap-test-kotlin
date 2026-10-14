package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual camera event summary is in its life cycle. */
enum class ManualCameraEventSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a manual camera event summary and its current status. */
data class ManualCameraEventSummary(
    var code: String,
    var name: String,
    var pallets: Int,
    var description: String,
    var odometer: Int,
    var version: Int,
    var status: ManualCameraEventSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCameraEventSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCameraEventSummaryStatus): Boolean {
        when (status) {
            ManualCameraEventSummaryStatus.NEW -> return next == ManualCameraEventSummaryStatus.DONE
            ManualCameraEventSummaryStatus.QUEUED -> return next == ManualCameraEventSummaryStatus.RUNNING
            ManualCameraEventSummaryStatus.RUNNING -> return next == ManualCameraEventSummaryStatus.DONE || next == ManualCameraEventSummaryStatus.FAILED
            ManualCameraEventSummaryStatus.DONE -> {
                when (next) {
                    ManualCameraEventSummaryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCameraEventSummaryStatus) {
        if (!canMoveTo(next)) {
            error("manual camera event summary $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"manual camera event summary"} $code"

    fun adjustedVolume(): Double = round((odometer.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): ManualCameraEventSummary {
        return ManualCameraEventSummary(
            code = code,
            name = name,
            pallets = pallets,
            description = description,
            odometer = odometer,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual camera event summary in status new. */
fun newManualCameraEventSummary(code: String, name: String): ManualCameraEventSummary {
    return ManualCameraEventSummary(
        code = code,
        name = name,
        pallets = 0,
        description = "",
        odometer = 0,
        version = 0,
        status = ManualCameraEventSummaryStatus.NEW,
    )
}
