package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling request is in its life cycle. */
enum class IdlingRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A idling request. Its code is unique within a tenant. */
data class IdlingRequest(
    var code: String,
    var label: String,
    var loadKg: Double,
    var title: String,
    var vehicleId: String,
    var externalRef: String,
    var revision: Int,
    var status: IdlingRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingRequestStatus): Boolean {
        when (status) {
            IdlingRequestStatus.DRAFT -> return next == IdlingRequestStatus.PENDING
            IdlingRequestStatus.PENDING -> {
                when (next) {
                    IdlingRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            IdlingRequestStatus.ACTIVE -> {
                when (next) {
                    IdlingRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            IdlingRequestStatus.SUSPENDED -> return next == IdlingRequestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: IdlingRequestStatus) {
        if (!canMoveTo(next)) {
            error("idling request $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedTotal(): Double = round(revision.toDouble() * loadKg * 100) / 100

    fun snapshot(): IdlingRequest {
        return IdlingRequest(
            code = code,
            label = label,
            loadKg = loadKg,
            title = title,
            vehicleId = vehicleId,
            externalRef = externalRef,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a idling request in status draft. */
fun newIdlingRequest(code: String, label: String): IdlingRequest {
    return IdlingRequest(
        code = code,
        label = label,
        loadKg = 0.0,
        title = "",
        vehicleId = "",
        externalRef = "",
        revision = 0,
        status = IdlingRequestStatus.DRAFT,
    )
}
