package com.acme.transport.emissions.evcharge.template

import com.acme.transport.emissions.clock.Clock

/** Moves default ev charge groups through their life cycle and keeps a journal of what it did. */
data class DefaultEvChargeGroupService(
    var store: DefaultEvChargeGroupStore,
    var validator: DefaultEvChargeGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultEvChargeGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default ev charge group $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultEvChargeGroupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default ev charge group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultEvChargeGroupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default ev charge group $code not found")
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

fun newDefaultEvChargeGroupService(store: DefaultEvChargeGroupStore, clock: Clock): DefaultEvChargeGroupService {
    return DefaultEvChargeGroupService(
        store = store,
        validator = defaultDefaultEvChargeGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultEvChargeGroupEngine(),
    )
}
