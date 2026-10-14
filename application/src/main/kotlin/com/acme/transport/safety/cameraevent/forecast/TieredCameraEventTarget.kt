package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered camera event target is in its life cycle. */
enum class TieredCameraEventTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tiered camera event target and its current status. */
data class TieredCameraEventTarget(
    var code: String,
    var label: String,
    var createdAt: Int,
    var rate: Double,
    var startsAt: Int,
    var pallets: Int,
    var etaMinutes: Int,
    var title: String,
    var phone: String,
    var category: String,
    var status: TieredCameraEventTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCameraEventTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCameraEventTargetStatus): Boolean {
        when (status) {
            TieredCameraEventTargetStatus.NEW -> {
                when (next) {
                    TieredCameraEventTargetStatus.QUEUED -> return true
                }
                return false
            }
            TieredCameraEventTargetStatus.QUEUED -> {
                when (next) {
                    TieredCameraEventTargetStatus.RUNNING, TieredCameraEventTargetStatus.DONE -> return true
                }
                return false
            }
            TieredCameraEventTargetStatus.RUNNING -> return next == TieredCameraEventTargetStatus.FAILED
            TieredCameraEventTargetStatus.DONE -> {
                when (next) {
                    TieredCameraEventTargetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCameraEventTargetStatus) {
        if (!canMoveTo(next)) {
            error("tiered camera event target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netVolume(): Double = round((etaMinutes.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): TieredCameraEventTarget {
        return TieredCameraEventTarget(
            code = code,
            label = label,
            createdAt = createdAt,
            rate = rate,
            startsAt = startsAt,
            pallets = pallets,
            etaMinutes = etaMinutes,
            title = title,
            phone = phone,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered camera event target in status new. */
fun newTieredCameraEventTarget(code: String, label: String): TieredCameraEventTarget {
    return TieredCameraEventTarget(
        code = code,
        label = label,
        createdAt = 0,
        rate = 0.0,
        startsAt = 0,
        pallets = 0,
        etaMinutes = 0,
        title = "",
        phone = "",
        category = "",
        status = TieredCameraEventTargetStatus.NEW,
    )
}
