package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tiered swap checkpoints through their life cycle and keeps a journal of what it did. */
data class TieredSwapCheckpointService(
    var store: TieredSwapCheckpointStore,
    var validator: TieredSwapCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredSwapCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered swap checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(TieredSwapCheckpointStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered swap checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredSwapCheckpointStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tiered swap checkpoint $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newTieredSwapCheckpointService(store: TieredSwapCheckpointStore, clock: Clock, flags: Registry): TieredSwapCheckpointService {
    return TieredSwapCheckpointService(
        store = store,
        validator = defaultTieredSwapCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredSwapCheckpointEngine(flags),
    )
}
