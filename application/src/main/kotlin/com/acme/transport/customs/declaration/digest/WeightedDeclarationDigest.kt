package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.text.normalize

/** Where a weighted declaration digest is in its life cycle. */
enum class WeightedDeclarationDigestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted declaration digest. Its code is unique within a tenant. */
data class WeightedDeclarationDigest(
    var code: String,
    var label: String,
    var country: String,
    var vehicleId: String,
    var pinned: Boolean,
    var durationMinutes: Int,
    var approved: Boolean,
    var summary: String,
    var status: WeightedDeclarationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDeclarationDigestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDeclarationDigestStatus): Boolean {
        when (status) {
            WeightedDeclarationDigestStatus.PROPOSED -> {
                when (next) {
                    WeightedDeclarationDigestStatus.APPROVED -> return true
                }
                return false
            }
            WeightedDeclarationDigestStatus.APPROVED -> return next == WeightedDeclarationDigestStatus.REJECTED || next == WeightedDeclarationDigestStatus.WITHDRAWN
            WeightedDeclarationDigestStatus.REJECTED -> return next == WeightedDeclarationDigestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedDeclarationDigestStatus) {
        if (!canMoveTo(next)) {
            error("weighted declaration digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): WeightedDeclarationDigest {
        return WeightedDeclarationDigest(
            code = code,
            label = label,
            country = country,
            vehicleId = vehicleId,
            pinned = pinned,
            durationMinutes = durationMinutes,
            approved = approved,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a weighted declaration digest in status proposed. */
fun newWeightedDeclarationDigest(code: String, label: String): WeightedDeclarationDigest {
    return WeightedDeclarationDigest(
        code = code,
        label = label,
        country = "",
        vehicleId = "",
        pinned = false,
        durationMinutes = 0,
        approved = false,
        summary = "",
        status = WeightedDeclarationDigestStatus.PROPOSED,
    )
}
