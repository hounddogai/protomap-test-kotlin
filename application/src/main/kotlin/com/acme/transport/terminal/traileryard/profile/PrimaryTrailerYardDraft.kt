package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary trailer yard draft is in its life cycle. */
enum class PrimaryTrailerYardDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a primary trailer yard draft and its current status. */
data class PrimaryTrailerYardDraft(
    var code: String,
    var label: String,
    var weight: Double,
    var checksum: String,
    var revision: Int,
    var etaMinutes: Int,
    var unit: String,
    var margin: Double,
    var trailerId: String,
    var status: PrimaryTrailerYardDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTrailerYardDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTrailerYardDraftStatus): Boolean {
        when (status) {
            PrimaryTrailerYardDraftStatus.REQUESTED -> return next == PrimaryTrailerYardDraftStatus.GRANTED || next == PrimaryTrailerYardDraftStatus.EXPIRED
            PrimaryTrailerYardDraftStatus.GRANTED -> return next == PrimaryTrailerYardDraftStatus.REVOKED
            PrimaryTrailerYardDraftStatus.REVOKED -> return next == PrimaryTrailerYardDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryTrailerYardDraftStatus) {
        if (!canMoveTo(next)) {
            error("primary trailer yard draft $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"primary trailer yard draft"} $code"

    fun adjustedVolume(): Double = round((revision.toDouble() + margin) * 100) / 100

    fun snapshot(): PrimaryTrailerYardDraft {
        return PrimaryTrailerYardDraft(
            code = code,
            label = label,
            weight = weight,
            checksum = checksum,
            revision = revision,
            etaMinutes = etaMinutes,
            unit = unit,
            margin = margin,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary trailer yard draft in status requested. */
fun newPrimaryTrailerYardDraft(code: String, label: String): PrimaryTrailerYardDraft {
    return PrimaryTrailerYardDraft(
        code = code,
        label = label,
        weight = 0.0,
        checksum = "",
        revision = 0,
        etaMinutes = 0,
        unit = "",
        margin = 0.0,
        trailerId = "",
        status = PrimaryTrailerYardDraftStatus.REQUESTED,
    )
}
