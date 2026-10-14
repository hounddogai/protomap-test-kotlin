package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive declaration segments through their life cycle and keeps a journal of what it did. */
data class AdaptiveDeclarationSegmentManager(
    var store: AdaptiveDeclarationSegmentStore,
    var validator: AdaptiveDeclarationSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveDeclarationSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive declaration segment $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDeclarationSegmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive declaration segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDeclarationSegmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive declaration segment $code not found")
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

fun newAdaptiveDeclarationSegmentManager(store: AdaptiveDeclarationSegmentStore, clock: Clock, flags: Registry): AdaptiveDeclarationSegmentManager {
    return AdaptiveDeclarationSegmentManager(
        store = store,
        validator = defaultAdaptiveDeclarationSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveDeclarationSegmentEngine(flags),
    )
}
