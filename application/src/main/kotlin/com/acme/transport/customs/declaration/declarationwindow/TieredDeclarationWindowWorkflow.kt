package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tiered declaration windows through their life cycle and keeps a journal of what it did. */
data class TieredDeclarationWindowService(
    var store: TieredDeclarationWindowStore,
    var validator: TieredDeclarationWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredDeclarationWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered declaration window $code not found")
        validator.requireValid(item)
        item.moveTo(TieredDeclarationWindowStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered declaration window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredDeclarationWindowStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered declaration window $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newTieredDeclarationWindowService(store: TieredDeclarationWindowStore, clock: Clock, flags: Registry): TieredDeclarationWindowService {
    return TieredDeclarationWindowService(
        store = store,
        validator = defaultTieredDeclarationWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredDeclarationWindowEngine(flags),
    )
}
