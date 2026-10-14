package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.clock.Clock

/** Moves qualification mappings through their life cycle and keeps a journal of what it did. */
data class QualificationMappingService(
    var store: QualificationMappingStore,
    var validator: QualificationMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("qualification mapping $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationMappingStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationMappingStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newQualificationMappingService(store: QualificationMappingStore, clock: Clock): QualificationMappingService {
    return QualificationMappingService(
        store = store,
        validator = defaultQualificationMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
