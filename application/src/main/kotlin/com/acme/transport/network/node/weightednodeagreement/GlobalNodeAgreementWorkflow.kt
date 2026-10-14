package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves global node agreements through their life cycle and keeps a journal of what it did. */
data class GlobalNodeAgreementDesk(
    var store: GlobalNodeAgreementStore,
    var validator: GlobalNodeAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalNodeAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global node agreement $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalNodeAgreementStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global node agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalNodeAgreementStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global node agreement $code not found")
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

fun newGlobalNodeAgreementDesk(store: GlobalNodeAgreementStore, clock: Clock, flags: Registry): GlobalNodeAgreementDesk {
    return GlobalNodeAgreementDesk(
        store = store,
        validator = defaultGlobalNodeAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalNodeAgreementEngine(flags),
    )
}
