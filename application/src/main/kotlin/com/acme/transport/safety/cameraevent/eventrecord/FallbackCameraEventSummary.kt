package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.kit.text.normalize

/** Where a fallback camera event summary is in its life cycle. */
enum class FallbackCameraEventSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback camera event summary as the camera event team keeps it. */
data class FallbackCameraEventSummary(
    var code: String,
    var label: String,
    var checksum: String,
    var timezone: String,
    var country: String,
    var quantity: Int,
    var status: FallbackCameraEventSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCameraEventSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCameraEventSummaryStatus): Boolean {
        when (status) {
            FallbackCameraEventSummaryStatus.OPEN -> return next == FallbackCameraEventSummaryStatus.ARCHIVED
            FallbackCameraEventSummaryStatus.IN_REVIEW -> {
                when (next) {
                    FallbackCameraEventSummaryStatus.RESOLVED, FallbackCameraEventSummaryStatus.REOPENED -> return true
                }
                return false
            }
            FallbackCameraEventSummaryStatus.RESOLVED -> {
                when (next) {
                    FallbackCameraEventSummaryStatus.REOPENED, FallbackCameraEventSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackCameraEventSummaryStatus.REOPENED -> {
                when (next) {
                    FallbackCameraEventSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCameraEventSummaryStatus) {
        if (!canMoveTo(next)) {
            error("fallback camera event summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): FallbackCameraEventSummary {
        return FallbackCameraEventSummary(
            code = code,
            label = label,
            checksum = checksum,
            timezone = timezone,
            country = country,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fallback camera event summary in status open. */
fun newFallbackCameraEventSummary(code: String, label: String): FallbackCameraEventSummary {
    return FallbackCameraEventSummary(
        code = code,
        label = label,
        checksum = "",
        timezone = "",
        country = "",
        quantity = 0,
        status = FallbackCameraEventSummaryStatus.OPEN,
    )
}
