package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves primary tender rollups through their life cycle and keeps a journal of what it did. */
data class PrimaryTenderRollupDesk(
    var store: PrimaryTenderRollupStore,
    var validator: PrimaryTenderRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryTenderRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary tender rollup $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTenderRollupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary tender rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTenderRollupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("primary tender rollup $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newPrimaryTenderRollupDesk(store: PrimaryTenderRollupStore, clock: Clock, flags: Registry): PrimaryTenderRollupDesk {
    return PrimaryTenderRollupDesk(
        store = store,
        validator = defaultPrimaryTenderRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryTenderRollupEngine(flags),
    )
}
