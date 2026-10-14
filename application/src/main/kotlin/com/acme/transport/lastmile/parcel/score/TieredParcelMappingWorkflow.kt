package com.acme.transport.lastmile.parcel.score

import com.acme.transport.lastmile.clock.Clock

/** Moves tiered parcel mappings through their life cycle and keeps a journal of what it did. */
data class TieredParcelMappingWorkflow(
    var store: TieredParcelMappingStore,
    var validator: TieredParcelMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredParcelMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered parcel mapping $code not found")
        validator.requireValid(item)
        item.moveTo(TieredParcelMappingStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered parcel mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredParcelMappingStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered parcel mapping $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newTieredParcelMappingWorkflow(store: TieredParcelMappingStore, clock: Clock): TieredParcelMappingWorkflow {
    return TieredParcelMappingWorkflow(
        store = store,
        validator = defaultTieredParcelMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredParcelMappingEngine(),
    )
}
