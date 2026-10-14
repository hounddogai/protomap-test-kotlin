package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.safety.clock.Clock

/** Moves near miss exceptions through their life cycle and keeps a journal of what it did. */
data class NearMissExceptionWorkflow(
    var store: NearMissExceptionStore,
    var validator: NearMissExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("near miss exception $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissExceptionStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissExceptionStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
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

fun newNearMissExceptionWorkflow(store: NearMissExceptionStore, clock: Clock): NearMissExceptionWorkflow {
    return NearMissExceptionWorkflow(
        store = store,
        validator = defaultNearMissExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
