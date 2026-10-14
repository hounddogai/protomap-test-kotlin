package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered relay overrides by code. */
interface TieredRelayOverrideStore {
    fun find(code: String): TieredRelayOverride?
    fun save(item: TieredRelayOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRelayOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRelayOverrideStore(
    var items: MutableMap<String, TieredRelayOverride>,
    var order: MutableList<String>,
) : TieredRelayOverrideStore {
    override fun find(code: String): TieredRelayOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRelayOverride) {
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

    override fun list(): MutableList<TieredRelayOverride> {
        val out = mutableListOf<TieredRelayOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRelayOverrideStore(): TieredRelayOverrideStore {
    return MemoryTieredRelayOverrideStore(mutableMapOf<String, TieredRelayOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRelayOverrideStore(
    var inner: TieredRelayOverrideStore,
    var cache: TtlCache<TieredRelayOverride>,
    var hits: Int,
    var misses: Int,
) : TieredRelayOverrideStore {
    override fun find(code: String): TieredRelayOverride? {
        val hit = (cache.get(code) as TieredRelayOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRelayOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRelayOverride> = inner.list()
}

fun newCachedTieredRelayOverrideStore(inner: TieredRelayOverrideStore, ttl: Int, clock: Clock): TieredRelayOverrideStore {
    return CachedTieredRelayOverrideStore(
        inner = inner,
        cache = newTtlCache<TieredRelayOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered relay overrides. Empty fields match all. */
data class TieredRelayOverrideQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRelayOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TieredRelayOverride>): MutableList<TieredRelayOverride> {
        val matched = filter(items) { candidate: TieredRelayOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRelayOverrides(store: TieredRelayOverrideStore, query: TieredRelayOverrideQuery): MutableList<TieredRelayOverride> {
    return query.apply(store.list())
}
