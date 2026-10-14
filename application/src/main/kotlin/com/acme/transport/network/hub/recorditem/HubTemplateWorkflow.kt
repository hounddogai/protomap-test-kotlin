package com.acme.transport.network.hub.recorditem

import com.acme.transport.network.clock.Clock

/** Moves hub templates through their life cycle and keeps a journal of what it did. */
data class HubTemplateDesk(
    var store: HubTemplateStore,
    var validator: HubTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("hub template $code not found")
        validator.requireValid(item)
        item.moveTo(HubTemplateStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("hub template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubTemplateStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("hub template $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newHubTemplateDesk(store: HubTemplateStore, clock: Clock): HubTemplateDesk {
    return HubTemplateDesk(
        store = store,
        validator = defaultHubTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubTemplateEngine(),
    )
}
