package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default roster agreements by code. */
interface DefaultRosterAgreementStore {
    fun find(code: String): DefaultRosterAgreement?
    fun save(item: DefaultRosterAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRosterAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRosterAgreementStore(
    var items: MutableMap<String, DefaultRosterAgreement>,
    var order: MutableList<String>,
) : DefaultRosterAgreementStore {
    override fun find(code: String): DefaultRosterAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRosterAgreement) {
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

    override fun list(): MutableList<DefaultRosterAgreement> {
        val out = mutableListOf<DefaultRosterAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRosterAgreementStore(): DefaultRosterAgreementStore {
    return MemoryDefaultRosterAgreementStore(
        mutableMapOf<String, DefaultRosterAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRosterAgreementStore(
    var inner: DefaultRosterAgreementStore,
    var cache: TtlCache<DefaultRosterAgreement>,
    var hits: Int,
    var misses: Int,
) : DefaultRosterAgreementStore {
    override fun find(code: String): DefaultRosterAgreement? {
        val hit = (cache.get(code) as DefaultRosterAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRosterAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRosterAgreement> = inner.list()
}

fun newCachedDefaultRosterAgreementStore(inner: DefaultRosterAgreementStore, ttl: Int, clock: Clock): DefaultRosterAgreementStore {
    return CachedDefaultRosterAgreementStore(
        inner = inner,
        cache = newTtlCache<DefaultRosterAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default roster agreements. Empty fields match all. */
data class DefaultRosterAgreementQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRosterAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRosterAgreement>): MutableList<DefaultRosterAgreement> {
        val matched = filter(items) { candidate: DefaultRosterAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRosterAgreements(store: DefaultRosterAgreementStore, query: DefaultRosterAgreementQuery): MutableList<DefaultRosterAgreement> {
    return query.apply(store.list())
}
