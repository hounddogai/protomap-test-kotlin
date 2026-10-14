package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.terminal.clock.Clock

/** Moves tiered cross dock mappings through their life cycle and keeps a journal of what it did. */
data class TieredCrossDockMappingDesk(
    var store: TieredCrossDockMappingStore,
    var validator: TieredCrossDockMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredCrossDockMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered cross dock mapping $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCrossDockMappingStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered cross dock mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCrossDockMappingStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered cross dock mapping $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newTieredCrossDockMappingDesk(store: TieredCrossDockMappingStore, clock: Clock): TieredCrossDockMappingDesk {
    return TieredCrossDockMappingDesk(
        store = store,
        validator = defaultTieredCrossDockMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredCrossDockMappingEngine(),
    )
}
