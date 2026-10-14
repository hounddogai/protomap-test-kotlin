package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel agreement is in its life cycle. */
enum class ParcelAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a parcel agreement and its current status. */
data class ParcelAgreement(
    var code: String,
    var title: String,
    var archived: Boolean,
    var margin: Double,
    var source: String,
    var batchSize: Int,
    var status: ParcelAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelAgreementStatus): Boolean {
        when (status) {
            ParcelAgreementStatus.NEW -> {
                when (next) {
                    ParcelAgreementStatus.RUNNING -> return true
                }
                return false
            }
            ParcelAgreementStatus.QUEUED -> {
                when (next) {
                    ParcelAgreementStatus.DONE -> return true
                }
                return false
            }
            ParcelAgreementStatus.RUNNING -> {
                when (next) {
                    ParcelAgreementStatus.DONE -> return true
                }
                return false
            }
            ParcelAgreementStatus.DONE -> {
                when (next) {
                    ParcelAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelAgreementStatus) {
        if (!canMoveTo(next)) {
            error("parcel agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveLoad(): Double = round((margin + batchSize.toDouble()) * 100) / 100

    fun snapshot(): ParcelAgreement {
        return ParcelAgreement(
            code = code,
            title = title,
            archived = archived,
            margin = margin,
            source = source,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a parcel agreement in status new. */
fun newParcelAgreement(code: String, title: String): ParcelAgreement {
    return ParcelAgreement(
        code = code,
        title = title,
        archived = false,
        margin = 0.0,
        source = "",
        batchSize = 0,
        status = ParcelAgreementStatus.NEW,
    )
}
