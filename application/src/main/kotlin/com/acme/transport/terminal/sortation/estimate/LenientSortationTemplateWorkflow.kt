package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.terminal.clock.Clock

/** Moves lenient sortation templates through their life cycle and keeps a journal of what it did. */
data class LenientSortationTemplateDesk(
    var store: LenientSortationTemplateStore,
    var validator: LenientSortationTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient sortation template $code not found")
        validator.requireValid(item)
        item.moveTo(LenientSortationTemplateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient sortation template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientSortationTemplateStatus.FAILED)
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

fun newLenientSortationTemplateDesk(store: LenientSortationTemplateStore, clock: Clock): LenientSortationTemplateDesk {
    return LenientSortationTemplateDesk(
        store = store,
        validator = defaultLenientSortationTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
