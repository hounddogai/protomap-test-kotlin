package com.acme.transport.safety.scorecard.scorecardbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary scorecard bundle records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a primary scorecard bundle is in its life cycle. */
enum class PrimaryScorecardBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a primary scorecard bundle and its current status. */
data class PrimaryScorecardBundle(
    var code: String,
    var title: String,
    var segment: String,
    var retries: Int,
    var pallets: Int,
    var maximum: Double,
    var archived: Boolean,
    var category: String,
    var etaMinutes: Int,
    var channel: String,
    var status: PrimaryScorecardBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryScorecardBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryScorecardBundleStatus): Boolean {
        when (status) {
            PrimaryScorecardBundleStatus.REQUESTED -> return next == PrimaryScorecardBundleStatus.REVOKED
            PrimaryScorecardBundleStatus.GRANTED -> return next == PrimaryScorecardBundleStatus.EXPIRED
            PrimaryScorecardBundleStatus.REVOKED -> return next == PrimaryScorecardBundleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryScorecardBundleStatus) {
        if (!canMoveTo(next)) {
            error("primary scorecard bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round((maximum + retries.toDouble()) * 100) / 100

    fun snapshot(): PrimaryScorecardBundle {
        return PrimaryScorecardBundle(
            code = code,
            title = title,
            segment = segment,
            retries = retries,
            pallets = pallets,
            maximum = maximum,
            archived = archived,
            category = category,
            etaMinutes = etaMinutes,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary scorecard bundle in status requested. */
fun newPrimaryScorecardBundle(code: String, title: String): PrimaryScorecardBundle {
    return PrimaryScorecardBundle(
        code = code,
        title = title,
        segment = "",
        retries = 0,
        pallets = 0,
        maximum = 0.0,
        archived = false,
        category = "",
        etaMinutes = 0,
        channel = "",
        status = PrimaryScorecardBundleStatus.REQUESTED,
    )
}
