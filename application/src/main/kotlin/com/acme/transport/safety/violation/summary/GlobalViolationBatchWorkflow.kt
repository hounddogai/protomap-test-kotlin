package com.acme.transport.safety.violation.summary

import com.acme.transport.safety.clock.Clock

/** Moves global violation batches through their life cycle and keeps a journal of what it did. */
data class GlobalViolationBatchService(
    var store: GlobalViolationBatchStore,
    var validator: GlobalViolationBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalViolationBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global violation batch $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalViolationBatchStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global violation batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalViolationBatchStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global violation batch $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newGlobalViolationBatchService(store: GlobalViolationBatchStore, clock: Clock): GlobalViolationBatchService {
    return GlobalViolationBatchService(
        store = store,
        validator = defaultGlobalViolationBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalViolationBatchEngine(),
    )
}
