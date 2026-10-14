package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.text.normalize

/** Where a inspection request is in its life cycle. */
enum class InspectionRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection request. Its code is unique within a tenant. */
data class InspectionRequest(
    var code: String,
    var title: String,
    var carrier: String,
    var priority: Int,
    var description: String,
    var status: InspectionRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionRequestStatus): Boolean {
        when (status) {
            InspectionRequestStatus.DRAFT -> {
                when (next) {
                    InspectionRequestStatus.PENDING, InspectionRequestStatus.ACTIVE -> return true
                }
                return false
            }
            InspectionRequestStatus.PENDING -> return next == InspectionRequestStatus.CLOSED
            InspectionRequestStatus.ACTIVE -> return next == InspectionRequestStatus.SUSPENDED
            InspectionRequestStatus.SUSPENDED -> return next == InspectionRequestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: InspectionRequestStatus) {
        if (!canMoveTo(next)) {
            error("inspection request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection request"} $code"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): InspectionRequest {
        return InspectionRequest(
            code = code,
            title = title,
            carrier = carrier,
            priority = priority,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection request in status draft. */
fun newInspectionRequest(code: String, title: String): InspectionRequest {
    return InspectionRequest(
        code = code,
        title = title,
        carrier = "",
        priority = 0,
        description = "",
        status = InspectionRequestStatus.DRAFT,
    )
}
