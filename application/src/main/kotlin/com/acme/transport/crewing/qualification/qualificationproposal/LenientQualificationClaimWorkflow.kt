package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.crewing.clock.Clock

/** Moves lenient qualification claims through their life cycle and keeps a journal of what it did. */
data class LenientQualificationClaimDesk(
    var store: LenientQualificationClaimStore,
    var validator: LenientQualificationClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient qualification claim $code not found")
        validator.requireValid(item)
        item.moveTo(LenientQualificationClaimStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient qualification claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientQualificationClaimStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newLenientQualificationClaimDesk(store: LenientQualificationClaimStore, clock: Clock): LenientQualificationClaimDesk {
    return LenientQualificationClaimDesk(
        store = store,
        validator = defaultLenientQualificationClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
