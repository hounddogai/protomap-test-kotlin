package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.text.normalize

/** Where a camera event estimate is in its life cycle. */
enum class CameraEventEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A camera event estimate. Its code is unique within a tenant. */
data class CameraEventEstimate(
    var code: String,
    var name: String,
    var externalRef: String,
    var summary: String,
    var tags: MutableList<String>,
    var quantity: Int,
    var status: CameraEventEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventEstimateStatus): Boolean {
        when (status) {
            CameraEventEstimateStatus.CREATED -> {
                when (next) {
                    CameraEventEstimateStatus.VALIDATED, CameraEventEstimateStatus.PUBLISHED -> return true
                }
                return false
            }
            CameraEventEstimateStatus.VALIDATED -> {
                when (next) {
                    CameraEventEstimateStatus.PUBLISHED, CameraEventEstimateStatus.RETIRED -> return true
                }
                return false
            }
            CameraEventEstimateStatus.PUBLISHED -> {
                when (next) {
                    CameraEventEstimateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventEstimateStatus) {
        if (!canMoveTo(next)) {
            error("camera event estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): CameraEventEstimate {
        return CameraEventEstimate(
            code = code,
            name = name,
            externalRef = externalRef,
            summary = summary,
            tags = tags,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a camera event estimate in status created. */
fun newCameraEventEstimate(code: String, name: String): CameraEventEstimate {
    return CameraEventEstimate(
        code = code,
        name = name,
        externalRef = "",
        summary = "",
        tags = mutableListOf<String>(),
        quantity = 0,
        status = CameraEventEstimateStatus.CREATED,
    )
}
