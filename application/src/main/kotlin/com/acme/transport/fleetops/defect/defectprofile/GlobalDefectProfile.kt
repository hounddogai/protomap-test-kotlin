package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global defect profile records of fleetops's defect work, and the rules, stores, and calculations around
// them.

/** Where a global defect profile is in its life cycle. */
enum class GlobalDefectProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global defect profile as the defect team keeps it. */
data class GlobalDefectProfile(
    var code: String,
    var name: String,
    var endsAt: Int,
    var revision: Int,
    var quantity: Int,
    var contactEmail: String,
    var trailerId: String,
    var notes: String,
    var priority: Int,
    var stopCount: Int,
    var score: Double,
    var status: GlobalDefectProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDefectProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDefectProfileStatus): Boolean {
        when (status) {
            GlobalDefectProfileStatus.REQUESTED -> {
                when (next) {
                    GlobalDefectProfileStatus.REVOKED -> return true
                }
                return false
            }
            GlobalDefectProfileStatus.GRANTED -> {
                when (next) {
                    GlobalDefectProfileStatus.REVOKED -> return true
                }
                return false
            }
            GlobalDefectProfileStatus.REVOKED -> return next == GlobalDefectProfileStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalDefectProfileStatus) {
        if (!canMoveTo(next)) {
            error("global defect profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun weightedVolume(): Double = round((priority.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): GlobalDefectProfile {
        return GlobalDefectProfile(
            code = code,
            name = name,
            endsAt = endsAt,
            revision = revision,
            quantity = quantity,
            contactEmail = contactEmail,
            trailerId = trailerId,
            notes = notes,
            priority = priority,
            stopCount = stopCount,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global defect profile in status requested. */
fun newGlobalDefectProfile(code: String, name: String): GlobalDefectProfile {
    return GlobalDefectProfile(
        code = code,
        name = name,
        endsAt = 0,
        revision = 0,
        quantity = 0,
        contactEmail = "",
        trailerId = "",
        notes = "",
        priority = 0,
        stopCount = 0,
        score = 0.0,
        status = GlobalDefectProfileStatus.REQUESTED,
    )
}
