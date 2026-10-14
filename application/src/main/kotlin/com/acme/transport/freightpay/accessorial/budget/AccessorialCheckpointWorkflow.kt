package com.acme.transport.freightpay.accessorial.budget

import com.acme.transport.freightpay.clock.Clock

/** Moves accessorial checkpoints through their life cycle and keeps a journal of what it did. */
data class AccessorialCheckpointManager(
    var store: AccessorialCheckpointStore,
    var validator: AccessorialCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AccessorialCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("accessorial checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialCheckpointStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialCheckpointStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("accessorial checkpoint $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newAccessorialCheckpointManager(store: AccessorialCheckpointStore, clock: Clock): AccessorialCheckpointManager {
    return AccessorialCheckpointManager(
        store = store,
        validator = defaultAccessorialCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAccessorialCheckpointEngine(),
    )
}
