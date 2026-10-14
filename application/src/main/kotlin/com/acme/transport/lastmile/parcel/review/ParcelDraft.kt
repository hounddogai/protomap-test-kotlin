package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel draft is in its life cycle. */
enum class ParcelDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One parcel draft, with what the planners need to act on it. */
data class ParcelDraft(
    var code: String,
    var name: String,
    var amount: Double,
    var stopCount: Int,
    var checksum: String,
    var fuelLiters: Double,
    var label: String,
    var updatedAt: Int,
    var minimum: Double,
    var etaMinutes: Int,
    var endsAt: Int,
    var status: ParcelDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelDraftStatus): Boolean {
        when (status) {
            ParcelDraftStatus.NEW -> {
                when (next) {
                    ParcelDraftStatus.RUNNING -> return true
                }
                return false
            }
            ParcelDraftStatus.QUEUED -> {
                when (next) {
                    ParcelDraftStatus.DONE -> return true
                }
                return false
            }
            ParcelDraftStatus.RUNNING -> return next == ParcelDraftStatus.FAILED
            ParcelDraftStatus.DONE -> {
                when (next) {
                    ParcelDraftStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelDraftStatus) {
        if (!canMoveTo(next)) {
            error("parcel draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$name]"

    fun netVolume(): Double = round((endsAt.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ParcelDraft {
        return ParcelDraft(
            code = code,
            name = name,
            amount = amount,
            stopCount = stopCount,
            checksum = checksum,
            fuelLiters = fuelLiters,
            label = label,
            updatedAt = updatedAt,
            minimum = minimum,
            etaMinutes = etaMinutes,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a parcel draft in status new. */
fun newParcelDraft(code: String, name: String): ParcelDraft {
    return ParcelDraft(
        code = code,
        name = name,
        amount = 0.0,
        stopCount = 0,
        checksum = "",
        fuelLiters = 0.0,
        label = "",
        updatedAt = 0,
        minimum = 0.0,
        etaMinutes = 0,
        endsAt = 0,
        status = ParcelDraftStatus.NEW,
    )
}
