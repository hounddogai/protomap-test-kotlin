package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves lenient parcel proposals through their life cycle and keeps a journal of what it did. */
data class LenientParcelProposalService(
    var store: LenientParcelProposalStore,
    var validator: LenientParcelProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientParcelProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient parcel proposal $code not found")
        validator.requireValid(item)
        item.moveTo(LenientParcelProposalStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient parcel proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientParcelProposalStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient parcel proposal $code not found")
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

fun newLenientParcelProposalService(store: LenientParcelProposalStore, clock: Clock, flags: Registry): LenientParcelProposalService {
    return LenientParcelProposalService(
        store = store,
        validator = defaultLenientParcelProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientParcelProposalEngine(flags),
    )
}
