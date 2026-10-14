package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal drop agreement is in its life cycle. */
enum class SeasonalDropAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a seasonal drop agreement and its current status. */
data class SeasonalDropAgreement(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var archived: Boolean,
    var threshold: Double,
    var name: String,
    var margin: Double,
    var budget: Double,
    var tags: MutableList<String>,
    var weight: Double,
    var status: SeasonalDropAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDropAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDropAgreementStatus): Boolean {
        when (status) {
            SeasonalDropAgreementStatus.DRAFT -> {
                when (next) {
                    SeasonalDropAgreementStatus.PENDING -> return true
                }
                return false
            }
            SeasonalDropAgreementStatus.PENDING -> return next == SeasonalDropAgreementStatus.ACTIVE || next == SeasonalDropAgreementStatus.SUSPENDED
            SeasonalDropAgreementStatus.ACTIVE -> {
                when (next) {
                    SeasonalDropAgreementStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalDropAgreementStatus.SUSPENDED -> {
                when (next) {
                    SeasonalDropAgreementStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDropAgreementStatus) {
        if (!canMoveTo(next)) {
            error("seasonal drop agreement $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round((expiresAt.toDouble() - weight) * 100) / 100

    fun snapshot(): SeasonalDropAgreement {
        return SeasonalDropAgreement(
            code = code,
            label = label,
            expiresAt = expiresAt,
            archived = archived,
            threshold = threshold,
            name = name,
            margin = margin,
            budget = budget,
            tags = tags,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal drop agreement in status draft. */
fun newSeasonalDropAgreement(code: String, label: String): SeasonalDropAgreement {
    return SeasonalDropAgreement(
        code = code,
        label = label,
        expiresAt = 0,
        archived = false,
        threshold = 0.0,
        name = "",
        margin = 0.0,
        budget = 0.0,
        tags = mutableListOf<String>(),
        weight = 0.0,
        status = SeasonalDropAgreementStatus.DRAFT,
    )
}
