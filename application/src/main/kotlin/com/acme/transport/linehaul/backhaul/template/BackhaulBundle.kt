package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul bundle is in its life cycle. */
enum class BackhaulBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A backhaul bundle. Its code is unique within a tenant. */
data class BackhaulBundle(
    var code: String,
    var label: String,
    var ceiling: Double,
    var title: String,
    var timezone: String,
    var externalRef: String,
    var routeCode: String,
    var amount: Double,
    var dock: String,
    var fuelLiters: Double,
    var status: BackhaulBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulBundleStatus): Boolean {
        when (status) {
            BackhaulBundleStatus.OPEN -> {
                when (next) {
                    BackhaulBundleStatus.RESOLVED, BackhaulBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            BackhaulBundleStatus.IN_REVIEW -> {
                when (next) {
                    BackhaulBundleStatus.REOPENED -> return true
                }
                return false
            }
            BackhaulBundleStatus.RESOLVED -> return next == BackhaulBundleStatus.REOPENED
            BackhaulBundleStatus.REOPENED -> return next == BackhaulBundleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BackhaulBundleStatus) {
        if (!canMoveTo(next)) {
            error("backhaul bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"backhaul bundle"} $code"

    fun netVolume(): Double = round(amount * ceiling * 100) / 100

    fun snapshot(): BackhaulBundle {
        return BackhaulBundle(
            code = code,
            label = label,
            ceiling = ceiling,
            title = title,
            timezone = timezone,
            externalRef = externalRef,
            routeCode = routeCode,
            amount = amount,
            dock = dock,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a backhaul bundle in status open. */
fun newBackhaulBundle(code: String, label: String): BackhaulBundle {
    return BackhaulBundle(
        code = code,
        label = label,
        ceiling = 0.0,
        title = "",
        timezone = "",
        externalRef = "",
        routeCode = "",
        amount = 0.0,
        dock = "",
        fuelLiters = 0.0,
        status = BackhaulBundleStatus.OPEN,
    )
}
