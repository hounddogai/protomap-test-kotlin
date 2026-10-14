package com.acme.transport.freightpay.accessorial.manifest

import com.acme.transport.freightpay.clock.Clock

/** Moves accessorial manifests through their life cycle and keeps a journal of what it did. */
data class AccessorialManifestCoordinator(
    var store: AccessorialManifestStore,
    var validator: AccessorialManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("accessorial manifest $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialManifestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialManifestStatus.EXPIRED)
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

fun newAccessorialManifestCoordinator(store: AccessorialManifestStore, clock: Clock): AccessorialManifestCoordinator {
    return AccessorialManifestCoordinator(
        store = store,
        validator = defaultAccessorialManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
