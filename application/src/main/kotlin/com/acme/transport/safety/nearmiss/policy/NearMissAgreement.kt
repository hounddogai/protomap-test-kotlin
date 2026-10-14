package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss agreement is in its life cycle. */
enum class NearMissAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One near miss agreement, with what the planners need to act on it. */
data class NearMissAgreement(
    var code: String,
    var name: String,
    var lane: String,
    var tags: MutableList<String>,
    var quantity: Int,
    var floorValue: Double,
    var revision: Int,
    var label: String,
    var updatedAt: Int,
    var vehicleId: String,
    var stopCount: Int,
    var status: NearMissAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissAgreementStatus): Boolean {
        when (status) {
            NearMissAgreementStatus.NEW -> {
                when (next) {
                    NearMissAgreementStatus.RUNNING, NearMissAgreementStatus.FAILED -> return true
                }
                return false
            }
            NearMissAgreementStatus.QUEUED -> {
                when (next) {
                    NearMissAgreementStatus.FAILED -> return true
                }
                return false
            }
            NearMissAgreementStatus.RUNNING -> return next == NearMissAgreementStatus.FAILED
            NearMissAgreementStatus.DONE -> return next == NearMissAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: NearMissAgreementStatus) {
        if (!canMoveTo(next)) {
            error("near miss agreement $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"near miss agreement"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((stopCount.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): NearMissAgreement {
        return NearMissAgreement(
            code = code,
            name = name,
            lane = lane,
            tags = tags,
            quantity = quantity,
            floorValue = floorValue,
            revision = revision,
            label = label,
            updatedAt = updatedAt,
            vehicleId = vehicleId,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a near miss agreement in status new. */
fun newNearMissAgreement(code: String, name: String): NearMissAgreement {
    return NearMissAgreement(
        code = code,
        name = name,
        lane = "",
        tags = mutableListOf<String>(),
        quantity = 0,
        floorValue = 0.0,
        revision = 0,
        label = "",
        updatedAt = 0,
        vehicleId = "",
        stopCount = 0,
        status = NearMissAgreementStatus.NEW,
    )
}
