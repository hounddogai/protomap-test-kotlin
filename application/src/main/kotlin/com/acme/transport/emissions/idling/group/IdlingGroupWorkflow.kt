package com.acme.transport.emissions.idling.group

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves idling groups through their life cycle and keeps a journal of what it did. */
data class IdlingGroupService(
    var store: IdlingGroupStore,
    var validator: IdlingGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("idling group $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("idling group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("idling group $code not found")
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

fun newIdlingGroupService(store: IdlingGroupStore, clock: Clock, flags: Registry): IdlingGroupService {
    return IdlingGroupService(
        store = store,
        validator = defaultIdlingGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingGroupEngine(flags),
    )
}
