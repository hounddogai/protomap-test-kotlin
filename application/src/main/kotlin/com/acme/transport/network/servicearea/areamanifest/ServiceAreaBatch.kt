package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.text.normalize

/** Where a service area batch is in its life cycle. */
enum class ServiceAreaBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A service area batch. Its code is unique within a tenant. */
data class ServiceAreaBatch(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var notes: String,
    var status: ServiceAreaBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaBatchStatus): Boolean {
        when (status) {
            ServiceAreaBatchStatus.OPEN -> return next == ServiceAreaBatchStatus.IN_REVIEW || next == ServiceAreaBatchStatus.RESOLVED
            ServiceAreaBatchStatus.IN_REVIEW -> return next == ServiceAreaBatchStatus.RESOLVED || next == ServiceAreaBatchStatus.ARCHIVED
            ServiceAreaBatchStatus.RESOLVED -> {
                when (next) {
                    ServiceAreaBatchStatus.REOPENED -> return true
                }
                return false
            }
            ServiceAreaBatchStatus.REOPENED -> return next == ServiceAreaBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ServiceAreaBatchStatus) {
        if (!canMoveTo(next)) {
            error("service area batch $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"service area batch"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): ServiceAreaBatch {
        return ServiceAreaBatch(
            code = code,
            name = name,
            expiresAt = expiresAt,
            attributes = attributes,
            summary = summary,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service area batch in status open. */
fun newServiceAreaBatch(code: String, name: String): ServiceAreaBatch {
    return ServiceAreaBatch(
        code = code,
        name = name,
        expiresAt = 0,
        attributes = mutableMapOf<String, String>(),
        summary = "",
        notes = "",
        status = ServiceAreaBatchStatus.OPEN,
    )
}
