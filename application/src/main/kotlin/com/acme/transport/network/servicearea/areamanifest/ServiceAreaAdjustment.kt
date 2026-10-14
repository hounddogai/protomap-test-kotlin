package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area adjustment is in its life cycle. */
enum class ServiceAreaAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A service area adjustment. Its code is unique within a tenant. */
data class ServiceAreaAdjustment(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var budget: Double,
    var visible: Boolean,
    var limit: Int,
    var discount: Double,
    var expiresAt: Int,
    var ceiling: Double,
    var status: ServiceAreaAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaAdjustmentStatus): Boolean {
        when (status) {
            ServiceAreaAdjustmentStatus.NEW -> {
                when (next) {
                    ServiceAreaAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            ServiceAreaAdjustmentStatus.QUEUED -> {
                when (next) {
                    ServiceAreaAdjustmentStatus.DONE, ServiceAreaAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            ServiceAreaAdjustmentStatus.RUNNING -> {
                when (next) {
                    ServiceAreaAdjustmentStatus.DONE, ServiceAreaAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            ServiceAreaAdjustmentStatus.DONE -> return next == ServiceAreaAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ServiceAreaAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("service area adjustment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round(discount * budget * 100) / 100

    fun snapshot(): ServiceAreaAdjustment {
        return ServiceAreaAdjustment(
            code = code,
            title = title,
            attributes = attributes,
            budget = budget,
            visible = visible,
            limit = limit,
            discount = discount,
            expiresAt = expiresAt,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a service area adjustment in status new. */
fun newServiceAreaAdjustment(code: String, title: String): ServiceAreaAdjustment {
    return ServiceAreaAdjustment(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        budget = 0.0,
        visible = false,
        limit = 0,
        discount = 0.0,
        expiresAt = 0,
        ceiling = 0.0,
        status = ServiceAreaAdjustmentStatus.NEW,
    )
}
