package com.acme.transport.freightpay.audit.slot

import com.acme.transport.freightpay.clock.Clock

/** Moves legacy audit manifests through their life cycle and keeps a journal of what it did. */
data class LegacyAuditManifestDesk(
    var store: LegacyAuditManifestStore,
    var validator: LegacyAuditManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyAuditManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy audit manifest $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyAuditManifestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy audit manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyAuditManifestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy audit manifest $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newLegacyAuditManifestDesk(store: LegacyAuditManifestStore, clock: Clock): LegacyAuditManifestDesk {
    return LegacyAuditManifestDesk(
        store = store,
        validator = defaultLegacyAuditManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyAuditManifestEngine(),
    )
}
