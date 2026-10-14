package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped hub request is in its life cycle. */
enum class CappedHubRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped hub request. Its code is unique within a tenant. */
data class CappedHubRequest(
    var code: String,
    var label: String,
    var startsAt: Int,
    var contactEmail: String,
    var budget: Double,
    var cost: Double,
    var status: CappedHubRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedHubRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedHubRequestStatus): Boolean {
        when (status) {
            CappedHubRequestStatus.CREATED -> return next == CappedHubRequestStatus.PUBLISHED
            CappedHubRequestStatus.VALIDATED -> return next == CappedHubRequestStatus.PUBLISHED || next == CappedHubRequestStatus.RETIRED
            CappedHubRequestStatus.PUBLISHED -> return next == CappedHubRequestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedHubRequestStatus) {
        if (!canMoveTo(next)) {
            error("capped hub request $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capped hub request"} $code"

    fun projectedValue(): Double = round((cost - startsAt.toDouble()) * 100) / 100

    fun snapshot(): CappedHubRequest {
        return CappedHubRequest(
            code = code,
            label = label,
            startsAt = startsAt,
            contactEmail = contactEmail,
            budget = budget,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped hub request in status created. */
fun newCappedHubRequest(code: String, label: String): CappedHubRequest {
    return CappedHubRequest(
        code = code,
        label = label,
        startsAt = 0,
        contactEmail = "",
        budget = 0.0,
        cost = 0.0,
        status = CappedHubRequestStatus.CREATED,
    )
}
