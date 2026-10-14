package com.acme.transport.crewing.qualification.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification preference records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a qualification preference is in its life cycle. */
enum class QualificationPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A qualification preference as the qualification team keeps it. */
data class QualificationPreference(
    var code: String,
    var label: String,
    var threshold: Double,
    var expiresAt: Int,
    var country: String,
    var revision: Int,
    var lane: String,
    var distanceKm: Double,
    var amount: Double,
    var region: String,
    var archived: Boolean,
    var status: QualificationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationPreferenceStatus): Boolean {
        when (status) {
            QualificationPreferenceStatus.IDLE -> return next == QualificationPreferenceStatus.WARMING || next == QualificationPreferenceStatus.READY
            QualificationPreferenceStatus.WARMING -> {
                when (next) {
                    QualificationPreferenceStatus.DRAINING, QualificationPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            QualificationPreferenceStatus.READY -> {
                when (next) {
                    QualificationPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            QualificationPreferenceStatus.DRAINING -> return next == QualificationPreferenceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: QualificationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("qualification preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"qualification preference"} $code"

    fun netExposure(): Double = round(threshold * distanceKm * 100) / 100

    fun snapshot(): QualificationPreference {
        return QualificationPreference(
            code = code,
            label = label,
            threshold = threshold,
            expiresAt = expiresAt,
            country = country,
            revision = revision,
            lane = lane,
            distanceKm = distanceKm,
            amount = amount,
            region = region,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a qualification preference in status idle. */
fun newQualificationPreference(code: String, label: String): QualificationPreference {
    return QualificationPreference(
        code = code,
        label = label,
        threshold = 0.0,
        expiresAt = 0,
        country = "",
        revision = 0,
        lane = "",
        distanceKm = 0.0,
        amount = 0.0,
        region = "",
        archived = false,
        status = QualificationPreferenceStatus.IDLE,
    )
}
