package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel mappings through their life cycle and keeps a journal of what it did. */
data class ParcelMappingManager(
    var store: ParcelMappingStore,
    var validator: ParcelMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("parcel mapping $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelMappingStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newParcelMappingManager(store: ParcelMappingStore, clock: Clock): ParcelMappingManager {
    return ParcelMappingManager(
        store = store,
        validator = defaultParcelMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
