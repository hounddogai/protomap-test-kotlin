package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.lastmile.clock.Clock

/** Moves lenient parcel policies through their life cycle and keeps a journal of what it did. */
data class LenientParcelPolicyWorkflow(
    var store: LenientParcelPolicyStore,
    var validator: LenientParcelPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient parcel policy $code not found")
        validator.requireValid(item)
        item.moveTo(LenientParcelPolicyStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient parcel policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientParcelPolicyStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientParcelPolicyWorkflow(store: LenientParcelPolicyStore, clock: Clock): LenientParcelPolicyWorkflow {
    return LenientParcelPolicyWorkflow(
        store = store,
        validator = defaultLenientParcelPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
