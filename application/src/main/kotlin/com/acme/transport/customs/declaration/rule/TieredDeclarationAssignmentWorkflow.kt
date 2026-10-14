package com.acme.transport.customs.declaration.rule

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves tiered declaration assignments through their life cycle and keeps a journal of what it did.
 */
data class TieredDeclarationAssignmentManager(
    var store: TieredDeclarationAssignmentStore,
    var validator: TieredDeclarationAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredDeclarationAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered declaration assignment $code not found")
        validator.requireValid(item)
        item.moveTo(TieredDeclarationAssignmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered declaration assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredDeclarationAssignmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tiered declaration assignment $code not found")
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

fun newTieredDeclarationAssignmentManager(store: TieredDeclarationAssignmentStore, clock: Clock, flags: Registry): TieredDeclarationAssignmentManager {
    return TieredDeclarationAssignmentManager(
        store = store,
        validator = defaultTieredDeclarationAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredDeclarationAssignmentEngine(flags),
    )
}
