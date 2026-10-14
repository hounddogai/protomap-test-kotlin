package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling tariff draft is in its life cycle. */
enum class RollingTariffDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling tariff draft as the tariff team keeps it. */
data class RollingTariffDraft(
    var code: String,
    var name: String,
    var version: Int,
    var floorValue: Double,
    var checksum: String,
    var label: String,
    var rate: Double,
    var etaMinutes: Int,
    var contactEmail: String,
    var region: String,
    var status: RollingTariffDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTariffDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTariffDraftStatus): Boolean {
        when (status) {
            RollingTariffDraftStatus.PROPOSED -> return next == RollingTariffDraftStatus.APPROVED || next == RollingTariffDraftStatus.REJECTED
            RollingTariffDraftStatus.APPROVED -> {
                when (next) {
                    RollingTariffDraftStatus.REJECTED, RollingTariffDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingTariffDraftStatus.REJECTED -> {
                when (next) {
                    RollingTariffDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTariffDraftStatus) {
        if (!canMoveTo(next)) {
            error("rolling tariff draft $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($name)"

    fun projectedVolume(): Double = round((etaMinutes.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): RollingTariffDraft {
        return RollingTariffDraft(
            code = code,
            name = name,
            version = version,
            floorValue = floorValue,
            checksum = checksum,
            label = label,
            rate = rate,
            etaMinutes = etaMinutes,
            contactEmail = contactEmail,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling tariff draft in status proposed. */
fun newRollingTariffDraft(code: String, name: String): RollingTariffDraft {
    return RollingTariffDraft(
        code = code,
        name = name,
        version = 0,
        floorValue = 0.0,
        checksum = "",
        label = "",
        rate = 0.0,
        etaMinutes = 0,
        contactEmail = "",
        region = "",
        status = RollingTariffDraftStatus.PROPOSED,
    )
}
