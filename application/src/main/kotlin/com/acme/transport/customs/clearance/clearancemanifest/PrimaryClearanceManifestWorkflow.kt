package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.customs.clock.Clock

/** Moves primary clearance manifests through their life cycle and keeps a journal of what it did. */
data class PrimaryClearanceManifestWorkflow(
    var store: PrimaryClearanceManifestStore,
    var validator: PrimaryClearanceManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary clearance manifest $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryClearanceManifestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary clearance manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryClearanceManifestStatus.EXPIRED)
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

fun newPrimaryClearanceManifestWorkflow(store: PrimaryClearanceManifestStore, clock: Clock): PrimaryClearanceManifestWorkflow {
    return PrimaryClearanceManifestWorkflow(
        store = store,
        validator = defaultPrimaryClearanceManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
