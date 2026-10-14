package com.acme.transport.crewing.qualification.request

import com.acme.transport.crewing.clock.Clock

/**
 * Moves regional qualification windows through their life cycle and keeps a journal of what it did.
 */
data class RegionalQualificationWindowWorkflow(
    var store: RegionalQualificationWindowStore,
    var validator: RegionalQualificationWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional qualification window $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalQualificationWindowStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional qualification window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalQualificationWindowStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newRegionalQualificationWindowWorkflow(store: RegionalQualificationWindowStore, clock: Clock): RegionalQualificationWindowWorkflow {
    return RegionalQualificationWindowWorkflow(
        store = store,
        validator = defaultRegionalQualificationWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
