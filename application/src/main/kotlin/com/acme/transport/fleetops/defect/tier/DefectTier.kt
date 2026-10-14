package com.acme.transport.fleetops.defect.tier

import com.acme.transport.kit.text.normalize

// The defect tier records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect tier is in its life cycle. */
enum class DefectTierStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A defect tier. Its code is unique within a tenant. */
data class DefectTier(
    var code: String,
    var name: String,
    var retries: Int,
    var approved: Boolean,
    var label: String,
    var checksum: String,
    var status: DefectTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectTierStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectTierStatus): Boolean {
        when (status) {
            DefectTierStatus.DRAFT -> {
                when (next) {
                    DefectTierStatus.PENDING, DefectTierStatus.ACTIVE -> return true
                }
                return false
            }
            DefectTierStatus.PENDING -> {
                when (next) {
                    DefectTierStatus.ACTIVE, DefectTierStatus.CLOSED -> return true
                }
                return false
            }
            DefectTierStatus.ACTIVE -> return next == DefectTierStatus.CLOSED
            DefectTierStatus.SUSPENDED -> return next == DefectTierStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefectTierStatus) {
        if (!canMoveTo(next)) {
            error("defect tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"defect tier"} $code"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): DefectTier {
        return DefectTier(
            code = code,
            name = name,
            retries = retries,
            approved = approved,
            label = label,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a defect tier in status draft. */
fun newDefectTier(code: String, name: String): DefectTier {
    return DefectTier(
        code = code,
        name = name,
        retries = 0,
        approved = false,
        label = "",
        checksum = "",
        status = DefectTierStatus.DRAFT,
    )
}
