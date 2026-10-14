package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.clock.Clock

/** Moves global dispute batches through their life cycle and keeps a journal of what it did. */
data class GlobalDisputeBatchCoordinator(
    var store: GlobalDisputeBatchStore,
    var validator: GlobalDisputeBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalDisputeBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global dispute batch $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDisputeBatchStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global dispute batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDisputeBatchStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global dispute batch $code not found")
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

fun newGlobalDisputeBatchCoordinator(store: GlobalDisputeBatchStore, clock: Clock): GlobalDisputeBatchCoordinator {
    return GlobalDisputeBatchCoordinator(
        store = store,
        validator = defaultGlobalDisputeBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalDisputeBatchEngine(),
    )
}
