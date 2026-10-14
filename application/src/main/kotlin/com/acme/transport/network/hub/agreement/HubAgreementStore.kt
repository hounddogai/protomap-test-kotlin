package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub agreements by code. */
interface HubAgreementStore {
    fun find(code: String): HubAgreement?
    fun save(item: HubAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubAgreementStore(
    var items: MutableMap<String, HubAgreement>,
    var order: MutableList<String>,
) : HubAgreementStore {
    override fun find(code: String): HubAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubAgreement) {
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

    override fun list(): MutableList<HubAgreement> {
        val out = mutableListOf<HubAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubAgreementStore(): HubAgreementStore {
    return MemoryHubAgreementStore(mutableMapOf<String, HubAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubAgreementStore(
    var inner: HubAgreementStore,
    var cache: TtlCache<HubAgreement>,
    var hits: Int,
    var misses: Int,
) : HubAgreementStore {
    override fun find(code: String): HubAgreement? {
        val hit = (cache.get(code) as HubAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubAgreement> = inner.list()
}

fun newCachedHubAgreementStore(inner: HubAgreementStore, ttl: Int, clock: Clock): HubAgreementStore {
    return CachedHubAgreementStore(
        inner = inner,
        cache = newTtlCache<HubAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub agreements. Empty fields match all. */
data class HubAgreementQuery(
    var segment: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubAgreement): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubAgreement>): MutableList<HubAgreement> {
        val matched = filter(items) { candidate: HubAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubAgreements(store: HubAgreementStore, query: HubAgreementQuery): MutableList<HubAgreement> {
    return query.apply(store.list())
}
