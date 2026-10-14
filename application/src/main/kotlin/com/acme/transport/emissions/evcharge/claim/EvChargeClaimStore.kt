package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge claims by code. */
interface EvChargeClaimStore {
    fun find(code: String): EvChargeClaim?
    fun save(item: EvChargeClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeClaimStore(
    var items: MutableMap<String, EvChargeClaim>,
    var order: MutableList<String>,
) : EvChargeClaimStore {
    override fun find(code: String): EvChargeClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeClaim) {
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

    override fun list(): MutableList<EvChargeClaim> {
        val out = mutableListOf<EvChargeClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeClaimStore(): EvChargeClaimStore {
    return MemoryEvChargeClaimStore(mutableMapOf<String, EvChargeClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeClaimStore(
    var inner: EvChargeClaimStore,
    var cache: TtlCache<EvChargeClaim>,
    var hits: Int,
    var misses: Int,
) : EvChargeClaimStore {
    override fun find(code: String): EvChargeClaim? {
        val hit = (cache.get(code) as EvChargeClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeClaim> = inner.list()
}

fun newCachedEvChargeClaimStore(inner: EvChargeClaimStore, ttl: Int, clock: Clock): EvChargeClaimStore {
    return CachedEvChargeClaimStore(
        inner = inner,
        cache = newTtlCache<EvChargeClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge claims. Empty fields match all. */
data class EvChargeClaimQuery(
    var country: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeClaim): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeClaim>): MutableList<EvChargeClaim> {
        val matched = filter(items) { candidate: EvChargeClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeClaims(store: EvChargeClaimStore, query: EvChargeClaimQuery): MutableList<EvChargeClaim> {
    return query.apply(store.list())
}
