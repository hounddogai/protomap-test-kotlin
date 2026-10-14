package com.acme.transport.customs.declaration.summary

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves declaration summaries through their life cycle and keeps a journal of what it did. */
data class DeclarationSummaryDesk(
    var store: DeclarationSummaryStore,
    var validator: DeclarationSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("declaration summary $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationSummaryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationSummaryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("declaration summary $code not found")
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

fun newDeclarationSummaryDesk(store: DeclarationSummaryStore, clock: Clock, flags: Registry): DeclarationSummaryDesk {
    return DeclarationSummaryDesk(
        store = store,
        validator = defaultDeclarationSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationSummaryEngine(flags),
    )
}
