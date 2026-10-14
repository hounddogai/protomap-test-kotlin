package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break agreements by code. */
interface RestBreakAgreementStore {
    fun find(code: String): RestBreakAgreement?
    fun save(item: RestBreakAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakAgreementStore(
    var items: MutableMap<String, RestBreakAgreement>,
    var order: MutableList<String>,
) : RestBreakAgreementStore {
    override fun find(code: String): RestBreakAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakAgreement) {
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

    override fun list(): MutableList<RestBreakAgreement> {
        val out = mutableListOf<RestBreakAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakAgreementStore(): RestBreakAgreementStore {
    return MemoryRestBreakAgreementStore(mutableMapOf<String, RestBreakAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakAgreementStore(
    var inner: RestBreakAgreementStore,
    var cache: TtlCache<RestBreakAgreement>,
    var hits: Int,
    var misses: Int,
) : RestBreakAgreementStore {
    override fun find(code: String): RestBreakAgreement? {
        val hit = (cache.get(code) as RestBreakAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakAgreement> = inner.list()
}

fun newCachedRestBreakAgreementStore(inner: RestBreakAgreementStore, ttl: Int, clock: Clock): RestBreakAgreementStore {
    return CachedRestBreakAgreementStore(
        inner = inner,
        cache = newTtlCache<RestBreakAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break agreements. Empty fields match all. */
data class RestBreakAgreementQuery(
    var region: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakAgreement): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakAgreement>): MutableList<RestBreakAgreement> {
        val matched = filter(items) { candidate: RestBreakAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakAgreements(store: RestBreakAgreementStore, query: RestBreakAgreementQuery): MutableList<RestBreakAgreement> {
    return query.apply(store.list())
}
