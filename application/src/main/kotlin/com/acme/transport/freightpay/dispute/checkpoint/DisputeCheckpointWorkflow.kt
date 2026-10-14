package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute checkpoints through their life cycle and keeps a journal of what it did. */
data class DisputeCheckpointDesk(
    var store: DisputeCheckpointStore,
    var validator: DisputeCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dispute checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeCheckpointStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeCheckpointStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("dispute checkpoint $code not found")
        val result = engine.price(item)
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

fun newDisputeCheckpointDesk(store: DisputeCheckpointStore, clock: Clock): DisputeCheckpointDesk {
    return DisputeCheckpointDesk(
        store = store,
        validator = defaultDisputeCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeCheckpointEngine(),
    )
}
