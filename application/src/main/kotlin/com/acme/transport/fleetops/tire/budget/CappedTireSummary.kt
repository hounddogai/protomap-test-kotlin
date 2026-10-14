package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped tire summary is in its life cycle. */
enum class CappedTireSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped tire summary as the tire team keeps it. */
data class CappedTireSummary(
    var code: String,
    var title: String,
    var loadKg: Double,
    var updatedAt: Int,
    var unit: String,
    var notes: String,
    var label: String,
    var routeCode: String,
    var active: Boolean,
    var status: CappedTireSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTireSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTireSummaryStatus): Boolean {
        when (status) {
            CappedTireSummaryStatus.REQUESTED -> return next == CappedTireSummaryStatus.REVOKED || next == CappedTireSummaryStatus.EXPIRED
            CappedTireSummaryStatus.GRANTED -> {
                when (next) {
                    CappedTireSummaryStatus.EXPIRED -> return true
                }
                return false
            }
            CappedTireSummaryStatus.REVOKED -> return next == CappedTireSummaryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedTireSummaryStatus) {
        if (!canMoveTo(next)) {
            error("capped tire summary $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($title)"

    fun effectiveVolume(): Double = round(loadKg * updatedAt.toDouble() * 100) / 100

    fun snapshot(): CappedTireSummary {
        return CappedTireSummary(
            code = code,
            title = title,
            loadKg = loadKg,
            updatedAt = updatedAt,
            unit = unit,
            notes = notes,
            label = label,
            routeCode = routeCode,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capped tire summary in status requested. */
fun newCappedTireSummary(code: String, title: String): CappedTireSummary {
    return CappedTireSummary(
        code = code,
        title = title,
        loadKg = 0.0,
        updatedAt = 0,
        unit = "",
        notes = "",
        label = "",
        routeCode = "",
        active = false,
        status = CappedTireSummaryStatus.REQUESTED,
    )
}
