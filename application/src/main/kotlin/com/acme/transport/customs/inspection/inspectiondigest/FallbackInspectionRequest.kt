package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback inspection request is in its life cycle. */
enum class FallbackInspectionRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback inspection request. Its code is unique within a tenant. */
data class FallbackInspectionRequest(
    var code: String,
    var label: String,
    var score: Double,
    var attempts: Int,
    var priority: Int,
    var tags: MutableList<String>,
    var limit: Int,
    var depot: String,
    var createdAt: Int,
    var approved: Boolean,
    var amount: Double,
    var status: FallbackInspectionRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackInspectionRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackInspectionRequestStatus): Boolean {
        when (status) {
            FallbackInspectionRequestStatus.NEW -> {
                when (next) {
                    FallbackInspectionRequestStatus.QUEUED, FallbackInspectionRequestStatus.DONE -> return true
                }
                return false
            }
            FallbackInspectionRequestStatus.QUEUED -> {
                when (next) {
                    FallbackInspectionRequestStatus.RUNNING, FallbackInspectionRequestStatus.DONE -> return true
                }
                return false
            }
            FallbackInspectionRequestStatus.RUNNING -> return next == FallbackInspectionRequestStatus.DONE || next == FallbackInspectionRequestStatus.FAILED
            FallbackInspectionRequestStatus.DONE -> {
                when (next) {
                    FallbackInspectionRequestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackInspectionRequestStatus) {
        if (!canMoveTo(next)) {
            error("fallback inspection request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round((priority.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): FallbackInspectionRequest {
        return FallbackInspectionRequest(
            code = code,
            label = label,
            score = score,
            attempts = attempts,
            priority = priority,
            tags = tags,
            limit = limit,
            depot = depot,
            createdAt = createdAt,
            approved = approved,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback inspection request in status new. */
fun newFallbackInspectionRequest(code: String, label: String): FallbackInspectionRequest {
    return FallbackInspectionRequest(
        code = code,
        label = label,
        score = 0.0,
        attempts = 0,
        priority = 0,
        tags = mutableListOf<String>(),
        limit = 0,
        depot = "",
        createdAt = 0,
        approved = false,
        amount = 0.0,
        status = FallbackInspectionRequestStatus.NEW,
    )
}
