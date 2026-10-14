package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary qualification record is in its life cycle. */
enum class PrimaryQualificationRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary qualification record. Its code is unique within a tenant. */
data class PrimaryQualificationRecord(
    var code: String,
    var label: String,
    var region: String,
    var retries: Int,
    var approved: Boolean,
    var segment: String,
    var margin: Double,
    var dock: String,
    var driverId: String,
    var visible: Boolean,
    var status: PrimaryQualificationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryQualificationRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryQualificationRecordStatus): Boolean {
        when (status) {
            PrimaryQualificationRecordStatus.PLANNED -> return next == PrimaryQualificationRecordStatus.IN_PROGRESS || next == PrimaryQualificationRecordStatus.CANCELLED
            PrimaryQualificationRecordStatus.SCHEDULED -> return next == PrimaryQualificationRecordStatus.CANCELLED
            PrimaryQualificationRecordStatus.IN_PROGRESS -> return next == PrimaryQualificationRecordStatus.COMPLETED || next == PrimaryQualificationRecordStatus.CANCELLED
            PrimaryQualificationRecordStatus.COMPLETED -> return next == PrimaryQualificationRecordStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryQualificationRecordStatus) {
        if (!canMoveTo(next)) {
            error("primary qualification record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedVolume(): Double = round(retries.toDouble() * margin * 100) / 100

    fun snapshot(): PrimaryQualificationRecord {
        return PrimaryQualificationRecord(
            code = code,
            label = label,
            region = region,
            retries = retries,
            approved = approved,
            segment = segment,
            margin = margin,
            dock = dock,
            driverId = driverId,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a primary qualification record in status planned. */
fun newPrimaryQualificationRecord(code: String, label: String): PrimaryQualificationRecord {
    return PrimaryQualificationRecord(
        code = code,
        label = label,
        region = "",
        retries = 0,
        approved = false,
        segment = "",
        margin = 0.0,
        dock = "",
        driverId = "",
        visible = false,
        status = PrimaryQualificationRecordStatus.PLANNED,
    )
}
