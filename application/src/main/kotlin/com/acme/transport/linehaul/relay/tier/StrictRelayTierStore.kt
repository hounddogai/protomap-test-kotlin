package com.acme.transport.linehaul.relay.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict relay tiers by code. */
interface StrictRelayTierStore {
    fun find(code: String): StrictRelayTier?
    fun save(item: StrictRelayTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRelayTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRelayTierStore(
    var items: MutableMap<String, StrictRelayTier>,
    var order: MutableList<String>,
) : StrictRelayTierStore {
    override fun find(code: String): StrictRelayTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRelayTier) {
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

    override fun list(): MutableList<StrictRelayTier> {
        val out = mutableListOf<StrictRelayTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRelayTierStore(): StrictRelayTierStore {
    return MemoryStrictRelayTierStore(mutableMapOf<String, StrictRelayTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRelayTierStore(
    var inner: StrictRelayTierStore,
    var cache: TtlCache<StrictRelayTier>,
    var hits: Int,
    var misses: Int,
) : StrictRelayTierStore {
    override fun find(code: String): StrictRelayTier? {
        val hit = (cache.get(code) as StrictRelayTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRelayTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRelayTier> = inner.list()
}

fun newCachedStrictRelayTierStore(inner: StrictRelayTierStore, ttl: Int, clock: Clock): StrictRelayTierStore {
    return CachedStrictRelayTierStore(
        inner = inner,
        cache = newTtlCache<StrictRelayTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict relay tiers. Empty fields match all. */
data class StrictRelayTierQuery(
    var country: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRelayTier): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRelayTier>): MutableList<StrictRelayTier> {
        val matched = filter(items) { candidate: StrictRelayTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRelayTiers(store: StrictRelayTierStore, query: StrictRelayTierQuery): MutableList<StrictRelayTier> {
    return query.apply(store.list())
}
