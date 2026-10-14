package com.acme.transport.linehaul.relay.tier

import com.acme.transport.linehaul.clock.Clock

/** Moves default relay templates through their life cycle and keeps a journal of what it did. */
data class DefaultRelayTemplateManager(
    var store: DefaultRelayTemplateStore,
    var validator: DefaultRelayTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default relay template $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultRelayTemplateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default relay template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultRelayTemplateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newDefaultRelayTemplateManager(store: DefaultRelayTemplateStore, clock: Clock): DefaultRelayTemplateManager {
    return DefaultRelayTemplateManager(
        store = store,
        validator = defaultDefaultRelayTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
