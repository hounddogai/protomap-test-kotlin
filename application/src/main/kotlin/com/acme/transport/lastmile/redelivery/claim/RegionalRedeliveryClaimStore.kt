package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional redelivery claims by code. */
interface RegionalRedeliveryClaimStore {
    fun find(code: String): RegionalRedeliveryClaim?
    fun save(item: RegionalRedeliveryClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRedeliveryClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRedeliveryClaimStore(
    var items: MutableMap<String, RegionalRedeliveryClaim>,
    var order: MutableList<String>,
) : RegionalRedeliveryClaimStore {
    override fun find(code: String): RegionalRedeliveryClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRedeliveryClaim) {
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

    override fun list(): MutableList<RegionalRedeliveryClaim> {
        val out = mutableListOf<RegionalRedeliveryClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRedeliveryClaimStore(): RegionalRedeliveryClaimStore {
    return MemoryRegionalRedeliveryClaimStore(
        mutableMapOf<String, RegionalRedeliveryClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRedeliveryClaimStore(
    var inner: RegionalRedeliveryClaimStore,
    var cache: TtlCache<RegionalRedeliveryClaim>,
    var hits: Int,
    var misses: Int,
) : RegionalRedeliveryClaimStore {
    override fun find(code: String): RegionalRedeliveryClaim? {
        val hit = (cache.get(code) as RegionalRedeliveryClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRedeliveryClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRedeliveryClaim> = inner.list()
}

fun newCachedRegionalRedeliveryClaimStore(inner: RegionalRedeliveryClaimStore, ttl: Int, clock: Clock): RegionalRedeliveryClaimStore {
    return CachedRegionalRedeliveryClaimStore(
        inner = inner,
        cache = newTtlCache<RegionalRedeliveryClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional redelivery claims. Empty fields match all. */
data class RegionalRedeliveryClaimQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRedeliveryClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalRedeliveryClaim>): MutableList<RegionalRedeliveryClaim> {
        val matched = filter(items) { candidate: RegionalRedeliveryClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRedeliveryClaims(store: RegionalRedeliveryClaimStore, query: RegionalRedeliveryClaimQuery): MutableList<RegionalRedeliveryClaim> {
    return query.apply(store.list())
}
