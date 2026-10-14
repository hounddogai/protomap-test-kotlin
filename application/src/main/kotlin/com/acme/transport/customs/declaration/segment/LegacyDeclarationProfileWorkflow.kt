package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.clock.Clock

/** Moves legacy declaration profiles through their life cycle and keeps a journal of what it did. */
data class LegacyDeclarationProfileWorkflow(
    var store: LegacyDeclarationProfileStore,
    var validator: LegacyDeclarationProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy declaration profile $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyDeclarationProfileStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy declaration profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyDeclarationProfileStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newLegacyDeclarationProfileWorkflow(store: LegacyDeclarationProfileStore, clock: Clock): LegacyDeclarationProfileWorkflow {
    return LegacyDeclarationProfileWorkflow(
        store = store,
        validator = defaultLegacyDeclarationProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
