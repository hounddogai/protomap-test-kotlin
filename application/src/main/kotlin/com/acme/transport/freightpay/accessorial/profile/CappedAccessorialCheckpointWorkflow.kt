package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves capped accessorial checkpoints through their life cycle and keeps a journal of what it did.
 */
data class CappedAccessorialCheckpointWorkflow(
    var store: CappedAccessorialCheckpointStore,
    var validator: CappedAccessorialCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedAccessorialCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped accessorial checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(CappedAccessorialCheckpointStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped accessorial checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedAccessorialCheckpointStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped accessorial checkpoint $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newCappedAccessorialCheckpointWorkflow(store: CappedAccessorialCheckpointStore, clock: Clock): CappedAccessorialCheckpointWorkflow {
    return CappedAccessorialCheckpointWorkflow(
        store = store,
        validator = defaultCappedAccessorialCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedAccessorialCheckpointEngine(),
    )
}
