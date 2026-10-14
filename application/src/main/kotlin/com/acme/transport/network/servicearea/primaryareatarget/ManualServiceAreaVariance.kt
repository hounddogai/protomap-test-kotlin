package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual service area variance is in its life cycle. */
enum class ManualServiceAreaVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a manual service area variance and its current status. */
data class ManualServiceAreaVariance(
    var code: String,
    var name: String,
    var ownerId: String,
    var graceDays: Int,
    var discount: Double,
    var driverId: String,
    var summary: String,
    var attributes: MutableMap<String, String>,
    var description: String,
    var reviewer: String,
    var amount: Double,
    var status: ManualServiceAreaVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualServiceAreaVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualServiceAreaVarianceStatus): Boolean {
        when (status) {
            ManualServiceAreaVarianceStatus.NEW -> return next == ManualServiceAreaVarianceStatus.RUNNING || next == ManualServiceAreaVarianceStatus.DONE
            ManualServiceAreaVarianceStatus.QUEUED -> {
                when (next) {
                    ManualServiceAreaVarianceStatus.DONE -> return true
                }
                return false
            }
            ManualServiceAreaVarianceStatus.RUNNING -> {
                when (next) {
                    ManualServiceAreaVarianceStatus.DONE -> return true
                }
                return false
            }
            ManualServiceAreaVarianceStatus.DONE -> return next == ManualServiceAreaVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualServiceAreaVarianceStatus) {
        if (!canMoveTo(next)) {
            error("manual service area variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((discount - graceDays.toDouble()) * 100) / 100

    fun snapshot(): ManualServiceAreaVariance {
        return ManualServiceAreaVariance(
            code = code,
            name = name,
            ownerId = ownerId,
            graceDays = graceDays,
            discount = discount,
            driverId = driverId,
            summary = summary,
            attributes = attributes,
            description = description,
            reviewer = reviewer,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual service area variance in status new. */
fun newManualServiceAreaVariance(code: String, name: String): ManualServiceAreaVariance {
    return ManualServiceAreaVariance(
        code = code,
        name = name,
        ownerId = "",
        graceDays = 0,
        discount = 0.0,
        driverId = "",
        summary = "",
        attributes = mutableMapOf<String, String>(),
        description = "",
        reviewer = "",
        amount = 0.0,
        status = ManualServiceAreaVarianceStatus.NEW,
    )
}
