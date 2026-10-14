package com.acme.transport.network.hub.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional hub agreements by code. */
interface RegionalHubAgreementStore {
    fun find(code: String): RegionalHubAgreement?
    fun save(item: RegionalHubAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalHubAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalHubAgreementStore(
    var items: MutableMap<String, RegionalHubAgreement>,
    var order: MutableList<String>,
) : RegionalHubAgreementStore {
    override fun find(code: String): RegionalHubAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalHubAgreement) {
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

    override fun list(): MutableList<RegionalHubAgreement> {
        val out = mutableListOf<RegionalHubAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalHubAgreementStore(): RegionalHubAgreementStore {
    return MemoryRegionalHubAgreementStore(
        mutableMapOf<String, RegionalHubAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalHubAgreementStore(
    var inner: RegionalHubAgreementStore,
    var cache: TtlCache<RegionalHubAgreement>,
    var hits: Int,
    var misses: Int,
) : RegionalHubAgreementStore {
    override fun find(code: String): RegionalHubAgreement? {
        val hit = (cache.get(code) as RegionalHubAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalHubAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalHubAgreement> = inner.list()
}

fun newCachedRegionalHubAgreementStore(inner: RegionalHubAgreementStore, ttl: Int, clock: Clock): RegionalHubAgreementStore {
    return CachedRegionalHubAgreementStore(
        inner = inner,
        cache = newTtlCache<RegionalHubAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional hub agreements. Empty fields match all. */
data class RegionalHubAgreementQuery(
    var channel: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalHubAgreement): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalHubAgreement>): MutableList<RegionalHubAgreement> {
        val matched = filter(items) { candidate: RegionalHubAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalHubAgreements(store: RegionalHubAgreementStore, query: RegionalHubAgreementQuery): MutableList<RegionalHubAgreement> {
    return query.apply(store.list())
}
