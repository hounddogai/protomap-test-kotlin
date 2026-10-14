package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal cross dock score is in its life cycle. */
enum class SeasonalCrossDockScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A seasonal cross dock score as the cross dock team keeps it. */
data class SeasonalCrossDockScore(
    var code: String,
    var label: String,
    var sequence: Int,
    var active: Boolean,
    var archived: Boolean,
    var loadKg: Double,
    var floorValue: Double,
    var checksum: String,
    var status: SeasonalCrossDockScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCrossDockScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCrossDockScoreStatus): Boolean {
        when (status) {
            SeasonalCrossDockScoreStatus.CREATED -> return next == SeasonalCrossDockScoreStatus.PUBLISHED || next == SeasonalCrossDockScoreStatus.RETIRED
            SeasonalCrossDockScoreStatus.VALIDATED -> {
                when (next) {
                    SeasonalCrossDockScoreStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalCrossDockScoreStatus.PUBLISHED -> return next == SeasonalCrossDockScoreStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalCrossDockScoreStatus) {
        if (!canMoveTo(next)) {
            error("seasonal cross dock score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round((sequence.toDouble() - floorValue) * 100) / 100

    fun snapshot(): SeasonalCrossDockScore {
        return SeasonalCrossDockScore(
            code = code,
            label = label,
            sequence = sequence,
            active = active,
            archived = archived,
            loadKg = loadKg,
            floorValue = floorValue,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal cross dock score in status created. */
fun newSeasonalCrossDockScore(code: String, label: String): SeasonalCrossDockScore {
    return SeasonalCrossDockScore(
        code = code,
        label = label,
        sequence = 0,
        active = false,
        archived = false,
        loadKg = 0.0,
        floorValue = 0.0,
        checksum = "",
        status = SeasonalCrossDockScoreStatus.CREATED,
    )
}
