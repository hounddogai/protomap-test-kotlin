package com.acme.transport.network.cutoff.slot

import com.acme.transport.kit.text.normalize

/** Where a cutoff digest is in its life cycle. */
enum class CutoffDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A cutoff digest as the cutoff team keeps it. */
data class CutoffDigest(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var updatedAt: Int,
    var status: CutoffDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffDigestStatus): Boolean {
        when (status) {
            CutoffDigestStatus.IDLE -> {
                when (next) {
                    CutoffDigestStatus.WARMING, CutoffDigestStatus.DRAINING -> return true
                }
                return false
            }
            CutoffDigestStatus.WARMING -> {
                when (next) {
                    CutoffDigestStatus.READY -> return true
                }
                return false
            }
            CutoffDigestStatus.READY -> {
                when (next) {
                    CutoffDigestStatus.DRAINING, CutoffDigestStatus.STOPPED -> return true
                }
                return false
            }
            CutoffDigestStatus.DRAINING -> return next == CutoffDigestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CutoffDigestStatus) {
        if (!canMoveTo(next)) {
            error("cutoff digest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): CutoffDigest {
        return CutoffDigest(
            code = code,
            name = name,
            attributes = attributes,
            summary = summary,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cutoff digest in status idle. */
fun newCutoffDigest(code: String, name: String): CutoffDigest {
    return CutoffDigest(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        summary = "",
        updatedAt = 0,
        status = CutoffDigestStatus.IDLE,
    )
}
