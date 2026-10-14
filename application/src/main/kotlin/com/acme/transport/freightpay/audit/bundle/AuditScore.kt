package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit score is in its life cycle. */
enum class AuditScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One audit score, with what the planners need to act on it. */
data class AuditScore(
    var code: String,
    var label: String,
    var ratio: Double,
    var pallets: Int,
    var attributes: MutableMap<String, String>,
    var status: AuditScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditScoreStatus): Boolean {
        when (status) {
            AuditScoreStatus.NEW -> {
                when (next) {
                    AuditScoreStatus.FAILED -> return true
                }
                return false
            }
            AuditScoreStatus.QUEUED -> return next == AuditScoreStatus.FAILED
            AuditScoreStatus.RUNNING -> {
                when (next) {
                    AuditScoreStatus.DONE -> return true
                }
                return false
            }
            AuditScoreStatus.DONE -> return next == AuditScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AuditScoreStatus) {
        if (!canMoveTo(next)) {
            error("audit score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"audit score"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((pallets.toDouble() - ratio) * 100) / 100

    fun snapshot(): AuditScore {
        return AuditScore(
            code = code,
            label = label,
            ratio = ratio,
            pallets = pallets,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a audit score in status new. */
fun newAuditScore(code: String, label: String): AuditScore {
    return AuditScore(
        code = code,
        label = label,
        ratio = 0.0,
        pallets = 0,
        attributes = mutableMapOf<String, String>(),
        status = AuditScoreStatus.NEW,
    )
}
