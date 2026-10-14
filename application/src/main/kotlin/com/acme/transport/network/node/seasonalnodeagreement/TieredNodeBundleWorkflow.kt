package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.network.clock.Clock

/** Moves tiered node bundles through their life cycle and keeps a journal of what it did. */
data class TieredNodeBundleDesk(
    var store: TieredNodeBundleStore,
    var validator: TieredNodeBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered node bundle $code not found")
        validator.requireValid(item)
        item.moveTo(TieredNodeBundleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered node bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredNodeBundleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newTieredNodeBundleDesk(store: TieredNodeBundleStore, clock: Clock): TieredNodeBundleDesk {
    return TieredNodeBundleDesk(
        store = store,
        validator = defaultTieredNodeBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
