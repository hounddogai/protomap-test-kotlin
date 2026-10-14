package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.customs.clock.Clock

/** Moves tiered declaration scores through their life cycle and keeps a journal of what it did. */
data class TieredDeclarationScoreService(
    var store: TieredDeclarationScoreStore,
    var validator: TieredDeclarationScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredDeclarationScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered declaration score $code not found")
        validator.requireValid(item)
        item.moveTo(TieredDeclarationScoreStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered declaration score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredDeclarationScoreStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered declaration score $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newTieredDeclarationScoreService(store: TieredDeclarationScoreStore, clock: Clock): TieredDeclarationScoreService {
    return TieredDeclarationScoreService(
        store = store,
        validator = defaultTieredDeclarationScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredDeclarationScoreEngine(),
    )
}
