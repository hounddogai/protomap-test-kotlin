package com.acme.transport.terminal.dwell.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dwell policy records of terminal's dwell work, and the rules, stores, and calculations around them.

/** Where a dwell policy is in its life cycle. */
enum class DwellPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A dwell policy. Its code is unique within a tenant. */
data class DwellPolicy(
    var code: String,
    var title: String,
    var retries: Int,
    var durationMinutes: Int,
    var startsAt: Int,
    var label: String,
    var externalRef: String,
    var minimum: Double,
    var batchSize: Int,
    var status: DwellPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellPolicyStatus): Boolean {
        when (status) {
            DwellPolicyStatus.CREATED -> {
                when (next) {
                    DwellPolicyStatus.VALIDATED, DwellPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            DwellPolicyStatus.VALIDATED -> {
                when (next) {
                    DwellPolicyStatus.PUBLISHED, DwellPolicyStatus.RETIRED -> return true
                }
                return false
            }
            DwellPolicyStatus.PUBLISHED -> {
                when (next) {
                    DwellPolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellPolicyStatus) {
        if (!canMoveTo(next)) {
            error("dwell policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $title"

    fun adjustedExposure(): Double = round(minimum * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): DwellPolicy {
        return DwellPolicy(
            code = code,
            title = title,
            retries = retries,
            durationMinutes = durationMinutes,
            startsAt = startsAt,
            label = label,
            externalRef = externalRef,
            minimum = minimum,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dwell policy in status created. */
fun newDwellPolicy(code: String, title: String): DwellPolicy {
    return DwellPolicy(
        code = code,
        title = title,
        retries = 0,
        durationMinutes = 0,
        startsAt = 0,
        label = "",
        externalRef = "",
        minimum = 0.0,
        batchSize = 0,
        status = DwellPolicyStatus.CREATED,
    )
}
