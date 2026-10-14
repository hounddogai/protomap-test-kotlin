package com.acme.transport.emissions.idling.estimate

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves idling bundles through their life cycle and keeps a journal of what it did. */
data class IdlingBundleService(
    var store: IdlingBundleStore,
    var validator: IdlingBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("idling bundle $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingBundleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("idling bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingBundleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("idling bundle $code not found")
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

fun newIdlingBundleService(store: IdlingBundleStore, clock: Clock, flags: Registry): IdlingBundleService {
    return IdlingBundleService(
        store = store,
        validator = defaultIdlingBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingBundleEngine(flags),
    )
}
