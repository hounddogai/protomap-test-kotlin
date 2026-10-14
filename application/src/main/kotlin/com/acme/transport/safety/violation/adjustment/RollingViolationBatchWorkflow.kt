package com.acme.transport.safety.violation.adjustment

import com.acme.transport.safety.clock.Clock

/** Moves rolling violation batches through their life cycle and keeps a journal of what it did. */
data class RollingViolationBatchCoordinator(
    var store: RollingViolationBatchStore,
    var validator: RollingViolationBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling violation batch $code not found")
        validator.requireValid(item)
        item.moveTo(RollingViolationBatchStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling violation batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingViolationBatchStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRollingViolationBatchCoordinator(store: RollingViolationBatchStore, clock: Clock): RollingViolationBatchCoordinator {
    return RollingViolationBatchCoordinator(
        store = store,
        validator = defaultRollingViolationBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
