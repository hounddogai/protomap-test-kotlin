package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered qualification profile is in its life cycle. */
enum class TieredQualificationProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tiered qualification profile, with what the planners need to act on it. */
data class TieredQualificationProfile(
    var code: String,
    var name: String,
    var score: Double,
    var etaMinutes: Int,
    var source: String,
    var approved: Boolean,
    var phone: String,
    var status: TieredQualificationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredQualificationProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredQualificationProfileStatus): Boolean {
        when (status) {
            TieredQualificationProfileStatus.PLANNED -> {
                when (next) {
                    TieredQualificationProfileStatus.IN_PROGRESS, TieredQualificationProfileStatus.COMPLETED -> return true
                }
                return false
            }
            TieredQualificationProfileStatus.SCHEDULED -> return next == TieredQualificationProfileStatus.IN_PROGRESS || next == TieredQualificationProfileStatus.CANCELLED
            TieredQualificationProfileStatus.IN_PROGRESS -> return next == TieredQualificationProfileStatus.COMPLETED || next == TieredQualificationProfileStatus.CANCELLED
            TieredQualificationProfileStatus.COMPLETED -> return next == TieredQualificationProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredQualificationProfileStatus) {
        if (!canMoveTo(next)) {
            error("tiered qualification profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered qualification profile"} $code"

    fun netVolume(): Double = round(etaMinutes.toDouble() * score * 100) / 100

    fun snapshot(): TieredQualificationProfile {
        return TieredQualificationProfile(
            code = code,
            name = name,
            score = score,
            etaMinutes = etaMinutes,
            source = source,
            approved = approved,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered qualification profile in status planned. */
fun newTieredQualificationProfile(code: String, name: String): TieredQualificationProfile {
    return TieredQualificationProfile(
        code = code,
        name = name,
        score = 0.0,
        etaMinutes = 0,
        source = "",
        approved = false,
        phone = "",
        status = TieredQualificationProfileStatus.PLANNED,
    )
}
