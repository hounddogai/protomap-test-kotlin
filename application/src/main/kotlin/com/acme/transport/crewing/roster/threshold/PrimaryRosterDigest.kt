package com.acme.transport.crewing.roster.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary roster digest is in its life cycle. */
enum class PrimaryRosterDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary roster digest. Its code is unique within a tenant. */
data class PrimaryRosterDigest(
    var code: String,
    var name: String,
    var minimum: Double,
    var capacity: Int,
    var vehicleId: String,
    var status: PrimaryRosterDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRosterDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRosterDigestStatus): Boolean {
        when (status) {
            PrimaryRosterDigestStatus.OPEN -> {
                when (next) {
                    PrimaryRosterDigestStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryRosterDigestStatus.IN_REVIEW -> return next == PrimaryRosterDigestStatus.RESOLVED || next == PrimaryRosterDigestStatus.ARCHIVED
            PrimaryRosterDigestStatus.RESOLVED -> {
                when (next) {
                    PrimaryRosterDigestStatus.REOPENED, PrimaryRosterDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryRosterDigestStatus.REOPENED -> {
                when (next) {
                    PrimaryRosterDigestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRosterDigestStatus) {
        if (!canMoveTo(next)) {
            error("primary roster digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round(minimum * capacity.toDouble() * 100) / 100

    fun snapshot(): PrimaryRosterDigest {
        return PrimaryRosterDigest(
            code = code,
            name = name,
            minimum = minimum,
            capacity = capacity,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary roster digest in status open. */
fun newPrimaryRosterDigest(code: String, name: String): PrimaryRosterDigest {
    return PrimaryRosterDigest(
        code = code,
        name = name,
        minimum = 0.0,
        capacity = 0,
        vehicleId = "",
        status = PrimaryRosterDigestStatus.OPEN,
    )
}
