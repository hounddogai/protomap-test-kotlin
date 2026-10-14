package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.lastmile.clock.Clock

/** Moves locker bundles through their life cycle and keeps a journal of what it did. */
data class LockerBundleService(
    var store: LockerBundleStore,
    var validator: LockerBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("locker bundle $code not found")
        validator.requireValid(item)
        item.moveTo(LockerBundleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("locker bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerBundleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newLockerBundleService(store: LockerBundleStore, clock: Clock): LockerBundleService {
    return LockerBundleService(
        store = store,
        validator = defaultLockerBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
