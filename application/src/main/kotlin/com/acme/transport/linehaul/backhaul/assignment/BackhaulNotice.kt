package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul notice is in its life cycle. */
enum class BackhaulNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A backhaul notice. Its code is unique within a tenant. */
data class BackhaulNotice(
    var code: String,
    var label: String,
    var description: String,
    var fuelLiters: Double,
    var category: String,
    var pinned: Boolean,
    var quantity: Int,
    var shiftCode: String,
    var discount: Double,
    var carrier: String,
    var distanceKm: Double,
    var status: BackhaulNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulNoticeStatus): Boolean {
        when (status) {
            BackhaulNoticeStatus.NEW -> return next == BackhaulNoticeStatus.DONE
            BackhaulNoticeStatus.QUEUED -> return next == BackhaulNoticeStatus.RUNNING || next == BackhaulNoticeStatus.FAILED
            BackhaulNoticeStatus.RUNNING -> {
                when (next) {
                    BackhaulNoticeStatus.DONE, BackhaulNoticeStatus.FAILED -> return true
                }
                return false
            }
            BackhaulNoticeStatus.DONE -> {
                when (next) {
                    BackhaulNoticeStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulNoticeStatus) {
        if (!canMoveTo(next)) {
            error("backhaul notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netLoad(): Double = round((distanceKm - discount) * 100) / 100

    fun snapshot(): BackhaulNotice {
        return BackhaulNotice(
            code = code,
            label = label,
            description = description,
            fuelLiters = fuelLiters,
            category = category,
            pinned = pinned,
            quantity = quantity,
            shiftCode = shiftCode,
            discount = discount,
            carrier = carrier,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a backhaul notice in status new. */
fun newBackhaulNotice(code: String, label: String): BackhaulNotice {
    return BackhaulNotice(
        code = code,
        label = label,
        description = "",
        fuelLiters = 0.0,
        category = "",
        pinned = false,
        quantity = 0,
        shiftCode = "",
        discount = 0.0,
        carrier = "",
        distanceKm = 0.0,
        status = BackhaulNoticeStatus.NEW,
    )
}
