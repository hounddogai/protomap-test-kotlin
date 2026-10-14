package com.acme.transport.customs.declaration.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient declaration policy is in its life cycle. */
enum class LenientDeclarationPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One lenient declaration policy, with what the planners need to act on it. */
data class LenientDeclarationPolicy(
    var code: String,
    var name: String,
    var endsAt: Int,
    var driverId: String,
    var ratio: Double,
    var approved: Boolean,
    var checksum: String,
    var visible: Boolean,
    var attributes: MutableMap<String, String>,
    var status: LenientDeclarationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDeclarationPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDeclarationPolicyStatus): Boolean {
        when (status) {
            LenientDeclarationPolicyStatus.NEW -> return next == LenientDeclarationPolicyStatus.QUEUED || next == LenientDeclarationPolicyStatus.DONE
            LenientDeclarationPolicyStatus.QUEUED -> return next == LenientDeclarationPolicyStatus.DONE || next == LenientDeclarationPolicyStatus.FAILED
            LenientDeclarationPolicyStatus.RUNNING -> {
                when (next) {
                    LenientDeclarationPolicyStatus.DONE, LenientDeclarationPolicyStatus.FAILED -> return true
                }
                return false
            }
            LenientDeclarationPolicyStatus.DONE -> {
                when (next) {
                    LenientDeclarationPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDeclarationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("lenient declaration policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient declaration policy"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((ratio - endsAt.toDouble()) * 100) / 100

    fun snapshot(): LenientDeclarationPolicy {
        return LenientDeclarationPolicy(
            code = code,
            name = name,
            endsAt = endsAt,
            driverId = driverId,
            ratio = ratio,
            approved = approved,
            checksum = checksum,
            visible = visible,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient declaration policy in status new. */
fun newLenientDeclarationPolicy(code: String, name: String): LenientDeclarationPolicy {
    return LenientDeclarationPolicy(
        code = code,
        name = name,
        endsAt = 0,
        driverId = "",
        ratio = 0.0,
        approved = false,
        checksum = "",
        visible = false,
        attributes = mutableMapOf<String, String>(),
        status = LenientDeclarationPolicyStatus.NEW,
    )
}
