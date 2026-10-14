package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual backhaul claim is in its life cycle. */
enum class ManualBackhaulClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual backhaul claim as the backhaul team keeps it. */
data class ManualBackhaulClaim(
    var code: String,
    var label: String,
    var maximum: Double,
    var ceiling: Double,
    var visible: Boolean,
    var sequence: Int,
    var contactEmail: String,
    var score: Double,
    var ratio: Double,
    var updatedAt: Int,
    var dock: String,
    var status: ManualBackhaulClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBackhaulClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBackhaulClaimStatus): Boolean {
        when (status) {
            ManualBackhaulClaimStatus.NEW -> {
                when (next) {
                    ManualBackhaulClaimStatus.RUNNING -> return true
                }
                return false
            }
            ManualBackhaulClaimStatus.QUEUED -> {
                when (next) {
                    ManualBackhaulClaimStatus.RUNNING, ManualBackhaulClaimStatus.FAILED -> return true
                }
                return false
            }
            ManualBackhaulClaimStatus.RUNNING -> {
                when (next) {
                    ManualBackhaulClaimStatus.DONE -> return true
                }
                return false
            }
            ManualBackhaulClaimStatus.DONE -> return next == ManualBackhaulClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualBackhaulClaimStatus) {
        if (!canMoveTo(next)) {
            error("manual backhaul claim $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedVolume(): Double = round((ceiling - sequence.toDouble()) * 100) / 100

    fun snapshot(): ManualBackhaulClaim {
        return ManualBackhaulClaim(
            code = code,
            label = label,
            maximum = maximum,
            ceiling = ceiling,
            visible = visible,
            sequence = sequence,
            contactEmail = contactEmail,
            score = score,
            ratio = ratio,
            updatedAt = updatedAt,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual backhaul claim in status new. */
fun newManualBackhaulClaim(code: String, label: String): ManualBackhaulClaim {
    return ManualBackhaulClaim(
        code = code,
        label = label,
        maximum = 0.0,
        ceiling = 0.0,
        visible = false,
        sequence = 0,
        contactEmail = "",
        score = 0.0,
        ratio = 0.0,
        updatedAt = 0,
        dock = "",
        status = ManualBackhaulClaimStatus.NEW,
    )
}
