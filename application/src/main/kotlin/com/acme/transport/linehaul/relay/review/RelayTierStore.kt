package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay tiers by code. */
interface RelayTierStore {
    fun find(code: String): RelayTier?
    fun save(item: RelayTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayTierStore(
    var items: MutableMap<String, RelayTier>,
    var order: MutableList<String>,
) : RelayTierStore {
    override fun find(code: String): RelayTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayTier) {
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

    override fun list(): MutableList<RelayTier> {
        val out = mutableListOf<RelayTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayTierStore(): RelayTierStore {
    return MemoryRelayTierStore(mutableMapOf<String, RelayTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayTierStore(
    var inner: RelayTierStore,
    var cache: TtlCache<RelayTier>,
    var hits: Int,
    var misses: Int,
) : RelayTierStore {
    override fun find(code: String): RelayTier? {
        val hit = (cache.get(code) as RelayTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayTier> = inner.list()
}

fun newCachedRelayTierStore(inner: RelayTierStore, ttl: Int, clock: Clock): RelayTierStore {
    return CachedRelayTierStore(
        inner = inner,
        cache = newTtlCache<RelayTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay tiers. Empty fields match all. */
data class RelayTierQuery(
    var country: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayTier): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<RelayTier>): MutableList<RelayTier> {
        val matched = filter(items) { candidate: RelayTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayTiers(store: RelayTierStore, query: RelayTierQuery): MutableList<RelayTier> {
    return query.apply(store.list())
}
