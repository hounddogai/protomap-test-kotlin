package com.acme.transport.customs.declaration.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling declaration manifest is in its life cycle. */
enum class RollingDeclarationManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rolling declaration manifest and its current status. */
data class RollingDeclarationManifest(
    var code: String,
    var name: String,
    var attempts: Int,
    var floorValue: Double,
    var revision: Int,
    var cost: Double,
    var summary: String,
    var weight: Double,
    var shiftCode: String,
    var category: String,
    var lane: String,
    var status: RollingDeclarationManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDeclarationManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDeclarationManifestStatus): Boolean {
        when (status) {
            RollingDeclarationManifestStatus.OPEN -> {
                when (next) {
                    RollingDeclarationManifestStatus.RESOLVED, RollingDeclarationManifestStatus.REOPENED -> return true
                }
                return false
            }
            RollingDeclarationManifestStatus.IN_REVIEW -> {
                when (next) {
                    RollingDeclarationManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingDeclarationManifestStatus.RESOLVED -> return next == RollingDeclarationManifestStatus.REOPENED || next == RollingDeclarationManifestStatus.ARCHIVED
            RollingDeclarationManifestStatus.REOPENED -> return next == RollingDeclarationManifestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingDeclarationManifestStatus) {
        if (!canMoveTo(next)) {
            error("rolling declaration manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedVolume(): Double = round(revision.toDouble() * cost * 100) / 100

    fun snapshot(): RollingDeclarationManifest {
        return RollingDeclarationManifest(
            code = code,
            name = name,
            attempts = attempts,
            floorValue = floorValue,
            revision = revision,
            cost = cost,
            summary = summary,
            weight = weight,
            shiftCode = shiftCode,
            category = category,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling declaration manifest in status open. */
fun newRollingDeclarationManifest(code: String, name: String): RollingDeclarationManifest {
    return RollingDeclarationManifest(
        code = code,
        name = name,
        attempts = 0,
        floorValue = 0.0,
        revision = 0,
        cost = 0.0,
        summary = "",
        weight = 0.0,
        shiftCode = "",
        category = "",
        lane = "",
        status = RollingDeclarationManifestStatus.OPEN,
    )
}
