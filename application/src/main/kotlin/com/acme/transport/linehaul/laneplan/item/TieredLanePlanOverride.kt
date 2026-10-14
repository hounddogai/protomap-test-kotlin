package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered lane plan override is in its life cycle. */
enum class TieredLanePlanOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered lane plan override as the lane plan team keeps it. */
data class TieredLanePlanOverride(
    var code: String,
    var title: String,
    var currency: String,
    var label: String,
    var category: String,
    var source: String,
    var loadKg: Double,
    var sequence: Int,
    var rate: Double,
    var channel: String,
    var ownerId: String,
    var status: TieredLanePlanOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredLanePlanOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredLanePlanOverrideStatus): Boolean {
        when (status) {
            TieredLanePlanOverrideStatus.NEW -> return next == TieredLanePlanOverrideStatus.QUEUED || next == TieredLanePlanOverrideStatus.DONE
            TieredLanePlanOverrideStatus.QUEUED -> {
                when (next) {
                    TieredLanePlanOverrideStatus.RUNNING, TieredLanePlanOverrideStatus.DONE -> return true
                }
                return false
            }
            TieredLanePlanOverrideStatus.RUNNING -> return next == TieredLanePlanOverrideStatus.DONE
            TieredLanePlanOverrideStatus.DONE -> {
                when (next) {
                    TieredLanePlanOverrideStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredLanePlanOverrideStatus) {
        if (!canMoveTo(next)) {
            error("tiered lane plan override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun adjustedValue(): Double = round((rate + loadKg) * 100) / 100

    fun snapshot(): TieredLanePlanOverride {
        return TieredLanePlanOverride(
            code = code,
            title = title,
            currency = currency,
            label = label,
            category = category,
            source = source,
            loadKg = loadKg,
            sequence = sequence,
            rate = rate,
            channel = channel,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered lane plan override in status new. */
fun newTieredLanePlanOverride(code: String, title: String): TieredLanePlanOverride {
    return TieredLanePlanOverride(
        code = code,
        title = title,
        currency = "",
        label = "",
        category = "",
        source = "",
        loadKg = 0.0,
        sequence = 0,
        rate = 0.0,
        channel = "",
        ownerId = "",
        status = TieredLanePlanOverrideStatus.NEW,
    )
}
