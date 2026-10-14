package com.acme.transport.safety.violation.profile

import com.acme.transport.safety.clock.Clock

/** Moves rolling violation profiles through their life cycle and keeps a journal of what it did. */
data class RollingViolationProfileService(
    var store: RollingViolationProfileStore,
    var validator: RollingViolationProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingViolationProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling violation profile $code not found")
        validator.requireValid(item)
        item.moveTo(RollingViolationProfileStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling violation profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingViolationProfileStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling violation profile $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newRollingViolationProfileService(store: RollingViolationProfileStore, clock: Clock): RollingViolationProfileService {
    return RollingViolationProfileService(
        store = store,
        validator = defaultRollingViolationProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingViolationProfileEngine(),
    )
}
