package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.clock.Clock

/** Moves regional declaration targets through their life cycle and keeps a journal of what it did. */
data class RegionalDeclarationTargetWorkflow(
    var store: RegionalDeclarationTargetStore,
    var validator: RegionalDeclarationTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalDeclarationTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional declaration target $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalDeclarationTargetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional declaration target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalDeclarationTargetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional declaration target $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newRegionalDeclarationTargetWorkflow(store: RegionalDeclarationTargetStore, clock: Clock): RegionalDeclarationTargetWorkflow {
    return RegionalDeclarationTargetWorkflow(
        store = store,
        validator = defaultRegionalDeclarationTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalDeclarationTargetEngine(),
    )
}
