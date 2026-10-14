package com.acme.transport.customs.hold.recorditem

import com.acme.transport.customs.clock.Clock

/** Moves lenient hold groups through their life cycle and keeps a journal of what it did. */
data class LenientHoldGroupService(
    var store: LenientHoldGroupStore,
    var validator: LenientHoldGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient hold group $code not found")
        validator.requireValid(item)
        item.moveTo(LenientHoldGroupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient hold group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientHoldGroupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newLenientHoldGroupService(store: LenientHoldGroupStore, clock: Clock): LenientHoldGroupService {
    return LenientHoldGroupService(
        store = store,
        validator = defaultLenientHoldGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
