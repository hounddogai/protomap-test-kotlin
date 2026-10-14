package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling declaration record is in its life cycle. */
enum class RollingDeclarationRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling declaration record. Its code is unique within a tenant. */
data class RollingDeclarationRecord(
    var code: String,
    var title: String,
    var loadKg: Double,
    var score: Double,
    var source: String,
    var archived: Boolean,
    var status: RollingDeclarationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDeclarationRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDeclarationRecordStatus): Boolean {
        when (status) {
            RollingDeclarationRecordStatus.REQUESTED -> return next == RollingDeclarationRecordStatus.EXPIRED
            RollingDeclarationRecordStatus.GRANTED -> {
                when (next) {
                    RollingDeclarationRecordStatus.EXPIRED -> return true
                }
                return false
            }
            RollingDeclarationRecordStatus.REVOKED -> return next == RollingDeclarationRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingDeclarationRecordStatus) {
        if (!canMoveTo(next)) {
            error("rolling declaration record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveTotal(): Double = round((loadKg - score) * 100) / 100

    fun snapshot(): RollingDeclarationRecord {
        return RollingDeclarationRecord(
            code = code,
            title = title,
            loadKg = loadKg,
            score = score,
            source = source,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling declaration record in status requested. */
fun newRollingDeclarationRecord(code: String, title: String): RollingDeclarationRecord {
    return RollingDeclarationRecord(
        code = code,
        title = title,
        loadKg = 0.0,
        score = 0.0,
        source = "",
        archived = false,
        status = RollingDeclarationRecordStatus.REQUESTED,
    )
}
