package com.acme.transport.crewing.qualification.qualificationoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive qualification override records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a adaptive qualification override is in its life cycle. */
enum class AdaptiveQualificationOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive qualification override. Its code is unique within a tenant. */
data class AdaptiveQualificationOverride(
    var code: String,
    var title: String,
    var lane: String,
    var routeCode: String,
    var retries: Int,
    var attributes: MutableMap<String, String>,
    var vehicleId: String,
    var threshold: Double,
    var dock: String,
    var status: AdaptiveQualificationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveQualificationOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveQualificationOverrideStatus): Boolean {
        when (status) {
            AdaptiveQualificationOverrideStatus.NEW -> return next == AdaptiveQualificationOverrideStatus.FAILED
            AdaptiveQualificationOverrideStatus.QUEUED -> return next == AdaptiveQualificationOverrideStatus.DONE || next == AdaptiveQualificationOverrideStatus.FAILED
            AdaptiveQualificationOverrideStatus.RUNNING -> {
                when (next) {
                    AdaptiveQualificationOverrideStatus.DONE, AdaptiveQualificationOverrideStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveQualificationOverrideStatus.DONE -> return next == AdaptiveQualificationOverrideStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveQualificationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("adaptive qualification override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round((threshold + retries.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveQualificationOverride {
        return AdaptiveQualificationOverride(
            code = code,
            title = title,
            lane = lane,
            routeCode = routeCode,
            retries = retries,
            attributes = attributes,
            vehicleId = vehicleId,
            threshold = threshold,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive qualification override in status new. */
fun newAdaptiveQualificationOverride(code: String, title: String): AdaptiveQualificationOverride {
    return AdaptiveQualificationOverride(
        code = code,
        title = title,
        lane = "",
        routeCode = "",
        retries = 0,
        attributes = mutableMapOf<String, String>(),
        vehicleId = "",
        threshold = 0.0,
        dock = "",
        status = AdaptiveQualificationOverrideStatus.NEW,
    )
}
