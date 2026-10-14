package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict roster agreements by code. */
interface StrictRosterAgreementStore {
    fun find(code: String): StrictRosterAgreement?
    fun save(item: StrictRosterAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRosterAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRosterAgreementStore(
    var items: MutableMap<String, StrictRosterAgreement>,
    var order: MutableList<String>,
) : StrictRosterAgreementStore {
    override fun find(code: String): StrictRosterAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRosterAgreement) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<StrictRosterAgreement> {
        val out = mutableListOf<StrictRosterAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRosterAgreementStore(): StrictRosterAgreementStore {
    return MemoryStrictRosterAgreementStore(
        mutableMapOf<String, StrictRosterAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRosterAgreementStore(
    var inner: StrictRosterAgreementStore,
    var cache: TtlCache<StrictRosterAgreement>,
    var hits: Int,
    var misses: Int,
) : StrictRosterAgreementStore {
    override fun find(code: String): StrictRosterAgreement? {
        val hit = (cache.get(code) as StrictRosterAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRosterAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRosterAgreement> = inner.list()
}

fun newCachedStrictRosterAgreementStore(inner: StrictRosterAgreementStore, ttl: Int, clock: Clock): StrictRosterAgreementStore {
    return CachedStrictRosterAgreementStore(
        inner = inner,
        cache = newTtlCache<StrictRosterAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict roster agreements. Empty fields match all. */
data class StrictRosterAgreementQuery(
    var segment: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRosterAgreement): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRosterAgreement>): MutableList<StrictRosterAgreement> {
        val matched = filter(items) { candidate: StrictRosterAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRosterAgreements(store: StrictRosterAgreementStore, query: StrictRosterAgreementQuery): MutableList<StrictRosterAgreement> {
    return query.apply(store.list())
}
