package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.text.normalize

/** Where a rolling tender manifest is in its life cycle. */
enum class RollingTenderManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling tender manifest. Its code is unique within a tenant. */
data class RollingTenderManifest(
    var code: String,
    var label: String,
    var channel: String,
    var lane: String,
    var updatedAt: Int,
    var status: RollingTenderManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTenderManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTenderManifestStatus): Boolean {
        when (status) {
            RollingTenderManifestStatus.CREATED -> return next == RollingTenderManifestStatus.VALIDATED
            RollingTenderManifestStatus.VALIDATED -> {
                when (next) {
                    RollingTenderManifestStatus.RETIRED -> return true
                }
                return false
            }
            RollingTenderManifestStatus.PUBLISHED -> {
                when (next) {
                    RollingTenderManifestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTenderManifestStatus) {
        if (!canMoveTo(next)) {
            error("rolling tender manifest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): RollingTenderManifest {
        return RollingTenderManifest(
            code = code,
            label = label,
            channel = channel,
            lane = lane,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rolling tender manifest in status created. */
fun newRollingTenderManifest(code: String, label: String): RollingTenderManifest {
    return RollingTenderManifest(
        code = code,
        label = label,
        channel = "",
        lane = "",
        updatedAt = 0,
        status = RollingTenderManifestStatus.CREATED,
    )
}
