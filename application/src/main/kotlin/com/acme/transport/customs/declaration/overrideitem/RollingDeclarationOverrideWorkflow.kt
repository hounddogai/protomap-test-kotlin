package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.customs.clock.Clock

/** Moves rolling declaration overrides through their life cycle and keeps a journal of what it did. */
data class RollingDeclarationOverrideService(
    var store: RollingDeclarationOverrideStore,
    var validator: RollingDeclarationOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling declaration override $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDeclarationOverrideStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling declaration override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDeclarationOverrideStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newRollingDeclarationOverrideService(store: RollingDeclarationOverrideStore, clock: Clock): RollingDeclarationOverrideService {
    return RollingDeclarationOverrideService(
        store = store,
        validator = defaultRollingDeclarationOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
