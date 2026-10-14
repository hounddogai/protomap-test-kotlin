package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves lenient lease return manifests through their life cycle and keeps a journal of what it did.
 */
data class LenientLeaseReturnManifestWorkflow(
    var store: LenientLeaseReturnManifestStore,
    var validator: LenientLeaseReturnManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientLeaseReturnManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient lease return manifest $code not found")
        validator.requireValid(item)
        item.moveTo(LenientLeaseReturnManifestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient lease return manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientLeaseReturnManifestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient lease return manifest $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newLenientLeaseReturnManifestWorkflow(store: LenientLeaseReturnManifestStore, clock: Clock): LenientLeaseReturnManifestWorkflow {
    return LenientLeaseReturnManifestWorkflow(
        store = store,
        validator = defaultLenientLeaseReturnManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientLeaseReturnManifestEngine(),
    )
}
