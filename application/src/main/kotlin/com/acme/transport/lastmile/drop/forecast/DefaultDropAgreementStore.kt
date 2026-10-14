package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default drop agreements by code. */
interface DefaultDropAgreementStore {
    fun find(code: String): DefaultDropAgreement?
    fun save(item: DefaultDropAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDropAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDropAgreementStore(
    var items: MutableMap<String, DefaultDropAgreement>,
    var order: MutableList<String>,
) : DefaultDropAgreementStore {
    override fun find(code: String): DefaultDropAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDropAgreement) {
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

    override fun list(): MutableList<DefaultDropAgreement> {
        val out = mutableListOf<DefaultDropAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDropAgreementStore(): DefaultDropAgreementStore {
    return MemoryDefaultDropAgreementStore(
        mutableMapOf<String, DefaultDropAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDropAgreementStore(
    var inner: DefaultDropAgreementStore,
    var cache: TtlCache<DefaultDropAgreement>,
    var hits: Int,
    var misses: Int,
) : DefaultDropAgreementStore {
    override fun find(code: String): DefaultDropAgreement? {
        val hit = (cache.get(code) as DefaultDropAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDropAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDropAgreement> = inner.list()
}

fun newCachedDefaultDropAgreementStore(inner: DefaultDropAgreementStore, ttl: Int, clock: Clock): DefaultDropAgreementStore {
    return CachedDefaultDropAgreementStore(
        inner = inner,
        cache = newTtlCache<DefaultDropAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default drop agreements. Empty fields match all. */
data class DefaultDropAgreementQuery(
    var lane: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDropAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDropAgreement>): MutableList<DefaultDropAgreement> {
        val matched = filter(items) { candidate: DefaultDropAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDropAgreements(store: DefaultDropAgreementStore, query: DefaultDropAgreementQuery): MutableList<DefaultDropAgreement> {
    return query.apply(store.list())
}
