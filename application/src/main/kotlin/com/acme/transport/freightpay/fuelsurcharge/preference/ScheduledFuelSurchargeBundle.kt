package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled fuel surcharge bundle is in its life cycle. */
enum class ScheduledFuelSurchargeBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a scheduled fuel surcharge bundle and its current status. */
data class ScheduledFuelSurchargeBundle(
    var code: String,
    var name: String,
    var externalRef: String,
    var tags: MutableList<String>,
    var archived: Boolean,
    var depot: String,
    var pinned: Boolean,
    var maximum: Double,
    var windowDays: Int,
    var category: String,
    var vehicleId: String,
    var status: ScheduledFuelSurchargeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFuelSurchargeBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFuelSurchargeBundleStatus): Boolean {
        when (status) {
            ScheduledFuelSurchargeBundleStatus.OPEN -> {
                when (next) {
                    ScheduledFuelSurchargeBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledFuelSurchargeBundleStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledFuelSurchargeBundleStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledFuelSurchargeBundleStatus.RESOLVED -> return next == ScheduledFuelSurchargeBundleStatus.ARCHIVED
            ScheduledFuelSurchargeBundleStatus.REOPENED -> {
                when (next) {
                    ScheduledFuelSurchargeBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledFuelSurchargeBundleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled fuel surcharge bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled fuel surcharge bundle"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((windowDays.toDouble() - maximum) * 100) / 100

    fun snapshot(): ScheduledFuelSurchargeBundle {
        return ScheduledFuelSurchargeBundle(
            code = code,
            name = name,
            externalRef = externalRef,
            tags = tags,
            archived = archived,
            depot = depot,
            pinned = pinned,
            maximum = maximum,
            windowDays = windowDays,
            category = category,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled fuel surcharge bundle in status open. */
fun newScheduledFuelSurchargeBundle(code: String, name: String): ScheduledFuelSurchargeBundle {
    return ScheduledFuelSurchargeBundle(
        code = code,
        name = name,
        externalRef = "",
        tags = mutableListOf<String>(),
        archived = false,
        depot = "",
        pinned = false,
        maximum = 0.0,
        windowDays = 0,
        category = "",
        vehicleId = "",
        status = ScheduledFuelSurchargeBundleStatus.OPEN,
    )
}
