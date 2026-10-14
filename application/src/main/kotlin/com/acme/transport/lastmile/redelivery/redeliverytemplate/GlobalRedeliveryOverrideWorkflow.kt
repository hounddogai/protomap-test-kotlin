package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves global redelivery overrides through their life cycle and keeps a journal of what it did. */
data class GlobalRedeliveryOverrideManager(
    var store: GlobalRedeliveryOverrideStore,
    var validator: GlobalRedeliveryOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalRedeliveryOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global redelivery override $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRedeliveryOverrideStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global redelivery override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRedeliveryOverrideStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global redelivery override $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newGlobalRedeliveryOverrideManager(store: GlobalRedeliveryOverrideStore, clock: Clock, flags: Registry): GlobalRedeliveryOverrideManager {
    return GlobalRedeliveryOverrideManager(
        store = store,
        validator = defaultGlobalRedeliveryOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalRedeliveryOverrideEngine(flags),
    )
}
