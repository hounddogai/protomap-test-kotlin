package com.acme.transport.network.servicearea.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area template is in its life cycle. */
enum class ServiceAreaTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A service area template as the service area team keeps it. */
data class ServiceAreaTemplate(
    var code: String,
    var title: String,
    var notes: String,
    var cost: Double,
    var description: String,
    var expiresAt: Int,
    var createdAt: Int,
    var status: ServiceAreaTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaTemplateStatus): Boolean {
        when (status) {
            ServiceAreaTemplateStatus.NEW -> {
                when (next) {
                    ServiceAreaTemplateStatus.QUEUED -> return true
                }
                return false
            }
            ServiceAreaTemplateStatus.QUEUED -> return next == ServiceAreaTemplateStatus.RUNNING || next == ServiceAreaTemplateStatus.DONE
            ServiceAreaTemplateStatus.RUNNING -> {
                when (next) {
                    ServiceAreaTemplateStatus.DONE -> return true
                }
                return false
            }
            ServiceAreaTemplateStatus.DONE -> return next == ServiceAreaTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ServiceAreaTemplateStatus) {
        if (!canMoveTo(next)) {
            error("service area template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"service area template"} $code"

    fun effectiveTotal(): Double = round((createdAt.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaTemplate {
        return ServiceAreaTemplate(
            code = code,
            title = title,
            notes = notes,
            cost = cost,
            description = description,
            expiresAt = expiresAt,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a service area template in status new. */
fun newServiceAreaTemplate(code: String, title: String): ServiceAreaTemplate {
    return ServiceAreaTemplate(
        code = code,
        title = title,
        notes = "",
        cost = 0.0,
        description = "",
        expiresAt = 0,
        createdAt = 0,
        status = ServiceAreaTemplateStatus.NEW,
    )
}
