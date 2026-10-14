package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.customs.clock.Clock

/** Moves primary hold mappings through their life cycle and keeps a journal of what it did. */
data class PrimaryHoldMappingDesk(
    var store: PrimaryHoldMappingStore,
    var validator: PrimaryHoldMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary hold mapping $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryHoldMappingStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary hold mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryHoldMappingStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newPrimaryHoldMappingDesk(store: PrimaryHoldMappingStore, clock: Clock): PrimaryHoldMappingDesk {
    return PrimaryHoldMappingDesk(
        store = store,
        validator = defaultPrimaryHoldMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
