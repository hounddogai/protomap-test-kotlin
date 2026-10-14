package com.acme.transport.safety.violation.entry

import com.acme.transport.safety.clock.Clock

/** Moves rolling violation claims through their life cycle and keeps a journal of what it did. */
data class RollingViolationClaimDesk(
    var store: RollingViolationClaimStore,
    var validator: RollingViolationClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingViolationClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling violation claim $code not found")
        validator.requireValid(item)
        item.moveTo(RollingViolationClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling violation claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingViolationClaimStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling violation claim $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
                continue
            }
            val problems = validator.validate(item)
            if (problems.size == 0) {
                continue
            }
            note("${item.code} needs attention: ${problems.joinToString(", ")}")
            changed += 1
        }
        return changed
    }
}

fun newRollingViolationClaimDesk(store: RollingViolationClaimStore, clock: Clock): RollingViolationClaimDesk {
    return RollingViolationClaimDesk(
        store = store,
        validator = defaultRollingViolationClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingViolationClaimEngine(),
    )
}
