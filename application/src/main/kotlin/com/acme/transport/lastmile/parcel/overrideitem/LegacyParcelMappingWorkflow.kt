package com.acme.transport.lastmile.parcel.overrideitem

import com.acme.transport.lastmile.clock.Clock

/** Moves legacy parcel mappings through their life cycle and keeps a journal of what it did. */
data class LegacyParcelMappingService(
    var store: LegacyParcelMappingStore,
    var validator: LegacyParcelMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyParcelMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy parcel mapping $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyParcelMappingStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy parcel mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyParcelMappingStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy parcel mapping $code not found")
        val result = engine.run(item)
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

fun newLegacyParcelMappingService(store: LegacyParcelMappingStore, clock: Clock): LegacyParcelMappingService {
    return LegacyParcelMappingService(
        store = store,
        validator = defaultLegacyParcelMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyParcelMappingEngine(),
    )
}
