package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.customs.clock.Clock

/**
 * Moves lenient declaration allocations through their life cycle and keeps a journal of what it did.
 */
data class LenientDeclarationAllocationManager(
    var store: LenientDeclarationAllocationStore,
    var validator: LenientDeclarationAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient declaration allocation $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDeclarationAllocationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient declaration allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDeclarationAllocationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientDeclarationAllocationManager(store: LenientDeclarationAllocationStore, clock: Clock): LenientDeclarationAllocationManager {
    return LenientDeclarationAllocationManager(
        store = store,
        validator = defaultLenientDeclarationAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
