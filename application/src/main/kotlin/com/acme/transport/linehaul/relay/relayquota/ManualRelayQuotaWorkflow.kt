package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.linehaul.clock.Clock

/** Moves manual relay quotas through their life cycle and keeps a journal of what it did. */
data class ManualRelayQuotaManager(
    var store: ManualRelayQuotaStore,
    var validator: ManualRelayQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual relay quota $code not found")
        validator.requireValid(item)
        item.moveTo(ManualRelayQuotaStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual relay quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualRelayQuotaStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newManualRelayQuotaManager(store: ManualRelayQuotaStore, clock: Clock): ManualRelayQuotaManager {
    return ManualRelayQuotaManager(
        store = store,
        validator = defaultManualRelayQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
