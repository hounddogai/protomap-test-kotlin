package com.acme.transport.linehaul.trailer.bundle

import com.acme.transport.linehaul.clock.Clock

/** Moves lenient trailer bundles through their life cycle and keeps a journal of what it did. */
data class LenientTrailerBundleDesk(
    var store: LenientTrailerBundleStore,
    var validator: LenientTrailerBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient trailer bundle $code not found")
        validator.requireValid(item)
        item.moveTo(LenientTrailerBundleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient trailer bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientTrailerBundleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newLenientTrailerBundleDesk(store: LenientTrailerBundleStore, clock: Clock): LenientTrailerBundleDesk {
    return LenientTrailerBundleDesk(
        store = store,
        validator = defaultLenientTrailerBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
