package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal qualification notice is in its life cycle. */
enum class SeasonalQualificationNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal qualification notice. Its code is unique within a tenant. */
data class SeasonalQualificationNotice(
    var code: String,
    var label: String,
    var odometer: Int,
    var region: String,
    var sequence: Int,
    var ratio: Double,
    var createdAt: Int,
    var reviewer: String,
    var score: Double,
    var margin: Double,
    var amount: Double,
    var status: SeasonalQualificationNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalQualificationNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalQualificationNoticeStatus): Boolean {
        when (status) {
            SeasonalQualificationNoticeStatus.IDLE -> {
                when (next) {
                    SeasonalQualificationNoticeStatus.DRAINING -> return true
                }
                return false
            }
            SeasonalQualificationNoticeStatus.WARMING -> {
                when (next) {
                    SeasonalQualificationNoticeStatus.READY, SeasonalQualificationNoticeStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalQualificationNoticeStatus.READY -> {
                when (next) {
                    SeasonalQualificationNoticeStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalQualificationNoticeStatus.DRAINING -> {
                when (next) {
                    SeasonalQualificationNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalQualificationNoticeStatus) {
        if (!canMoveTo(next)) {
            error("seasonal qualification notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"seasonal qualification notice"} $code"

    fun effectiveTotal(): Double = round((margin - odometer.toDouble()) * 100) / 100

    fun snapshot(): SeasonalQualificationNotice {
        return SeasonalQualificationNotice(
            code = code,
            label = label,
            odometer = odometer,
            region = region,
            sequence = sequence,
            ratio = ratio,
            createdAt = createdAt,
            reviewer = reviewer,
            score = score,
            margin = margin,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal qualification notice in status idle. */
fun newSeasonalQualificationNotice(code: String, label: String): SeasonalQualificationNotice {
    return SeasonalQualificationNotice(
        code = code,
        label = label,
        odometer = 0,
        region = "",
        sequence = 0,
        ratio = 0.0,
        createdAt = 0,
        reviewer = "",
        score = 0.0,
        margin = 0.0,
        amount = 0.0,
        status = SeasonalQualificationNoticeStatus.IDLE,
    )
}
