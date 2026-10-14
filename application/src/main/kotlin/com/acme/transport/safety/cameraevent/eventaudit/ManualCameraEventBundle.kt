package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.normalize

/** Where a manual camera event bundle is in its life cycle. */
enum class ManualCameraEventBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a manual camera event bundle and its current status. */
data class ManualCameraEventBundle(
    var code: String,
    var name: String,
    var dock: String,
    var driverId: String,
    var summary: String,
    var category: String,
    var visible: Boolean,
    var amount: Double,
    var status: ManualCameraEventBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCameraEventBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCameraEventBundleStatus): Boolean {
        when (status) {
            ManualCameraEventBundleStatus.NEW -> {
                when (next) {
                    ManualCameraEventBundleStatus.DONE -> return true
                }
                return false
            }
            ManualCameraEventBundleStatus.QUEUED -> {
                when (next) {
                    ManualCameraEventBundleStatus.RUNNING -> return true
                }
                return false
            }
            ManualCameraEventBundleStatus.RUNNING -> {
                when (next) {
                    ManualCameraEventBundleStatus.DONE, ManualCameraEventBundleStatus.FAILED -> return true
                }
                return false
            }
            ManualCameraEventBundleStatus.DONE -> return next == ManualCameraEventBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualCameraEventBundleStatus) {
        if (!canMoveTo(next)) {
            error("manual camera event bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = amount

    fun snapshot(): ManualCameraEventBundle {
        return ManualCameraEventBundle(
            code = code,
            name = name,
            dock = dock,
            driverId = driverId,
            summary = summary,
            category = category,
            visible = visible,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual camera event bundle in status new. */
fun newManualCameraEventBundle(code: String, name: String): ManualCameraEventBundle {
    return ManualCameraEventBundle(
        code = code,
        name = name,
        dock = "",
        driverId = "",
        summary = "",
        category = "",
        visible = false,
        amount = 0.0,
        status = ManualCameraEventBundleStatus.NEW,
    )
}
