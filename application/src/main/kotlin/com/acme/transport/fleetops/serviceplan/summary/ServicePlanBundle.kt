package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan bundle is in its life cycle. */
enum class ServicePlanBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A service plan bundle. Its code is unique within a tenant. */
data class ServicePlanBundle(
    var code: String,
    var name: String,
    var trailerId: String,
    var expiresAt: Int,
    var quantity: Int,
    var attributes: MutableMap<String, String>,
    var status: ServicePlanBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanBundleStatus): Boolean {
        when (status) {
            ServicePlanBundleStatus.NEW -> return next == ServicePlanBundleStatus.QUEUED
            ServicePlanBundleStatus.QUEUED -> return next == ServicePlanBundleStatus.RUNNING || next == ServicePlanBundleStatus.DONE
            ServicePlanBundleStatus.RUNNING -> return next == ServicePlanBundleStatus.DONE || next == ServicePlanBundleStatus.FAILED
            ServicePlanBundleStatus.DONE -> return next == ServicePlanBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ServicePlanBundleStatus) {
        if (!canMoveTo(next)) {
            error("service plan bundle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((quantity.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanBundle {
        return ServicePlanBundle(
            code = code,
            name = name,
            trailerId = trailerId,
            expiresAt = expiresAt,
            quantity = quantity,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service plan bundle in status new. */
fun newServicePlanBundle(code: String, name: String): ServicePlanBundle {
    return ServicePlanBundle(
        code = code,
        name = name,
        trailerId = "",
        expiresAt = 0,
        quantity = 0,
        attributes = mutableMapOf<String, String>(),
        status = ServicePlanBundleStatus.NEW,
    )
}
