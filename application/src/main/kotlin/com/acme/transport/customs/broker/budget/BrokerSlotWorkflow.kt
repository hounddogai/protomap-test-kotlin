package com.acme.transport.customs.broker.budget

import com.acme.transport.customs.clock.Clock

/** Moves broker slots through their life cycle and keeps a journal of what it did. */
data class BrokerSlotCoordinator(
    var store: BrokerSlotStore,
    var validator: BrokerSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BrokerSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("broker slot $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerSlotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("broker slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerSlotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("broker slot $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newBrokerSlotCoordinator(store: BrokerSlotStore, clock: Clock): BrokerSlotCoordinator {
    return BrokerSlotCoordinator(
        store = store,
        validator = defaultBrokerSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBrokerSlotEngine(),
    )
}
