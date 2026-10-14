package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.terminal.clock.Clock

/** Moves global trailer yard templates through their life cycle and keeps a journal of what it did. */
data class GlobalTrailerYardTemplateCoordinator(
    var store: GlobalTrailerYardTemplateStore,
    var validator: GlobalTrailerYardTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalTrailerYardTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global trailer yard template $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTrailerYardTemplateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global trailer yard template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTrailerYardTemplateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global trailer yard template $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newGlobalTrailerYardTemplateCoordinator(store: GlobalTrailerYardTemplateStore, clock: Clock): GlobalTrailerYardTemplateCoordinator {
    return GlobalTrailerYardTemplateCoordinator(
        store = store,
        validator = defaultGlobalTrailerYardTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalTrailerYardTemplateEngine(),
    )
}
