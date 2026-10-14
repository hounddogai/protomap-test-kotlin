package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.safety.clock.Clock

/** Moves near miss checkpoints through their life cycle and keeps a journal of what it did. */
data class NearMissCheckpointService(
    var store: NearMissCheckpointStore,
    var validator: NearMissCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NearMissCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("near miss checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissCheckpointStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissCheckpointStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("near miss checkpoint $code not found")
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

fun newNearMissCheckpointService(store: NearMissCheckpointStore, clock: Clock): NearMissCheckpointService {
    return NearMissCheckpointService(
        store = store,
        validator = defaultNearMissCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNearMissCheckpointEngine(),
    )
}
