package com.acme.transport.network.hub.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered hub baselines by code. */
interface TieredHubBaselineStore {
    fun find(code: String): TieredHubBaseline?
    fun save(item: TieredHubBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredHubBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredHubBaselineStore(
    var items: MutableMap<String, TieredHubBaseline>,
    var order: MutableList<String>,
) : TieredHubBaselineStore {
    override fun find(code: String): TieredHubBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredHubBaseline) {
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

    override fun list(): MutableList<TieredHubBaseline> {
        val out = mutableListOf<TieredHubBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredHubBaselineStore(): TieredHubBaselineStore {
    return MemoryTieredHubBaselineStore(mutableMapOf<String, TieredHubBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredHubBaselineStore(
    var inner: TieredHubBaselineStore,
    var cache: TtlCache<TieredHubBaseline>,
    var hits: Int,
    var misses: Int,
) : TieredHubBaselineStore {
    override fun find(code: String): TieredHubBaseline? {
        val hit = (cache.get(code) as TieredHubBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredHubBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredHubBaseline> = inner.list()
}

fun newCachedTieredHubBaselineStore(inner: TieredHubBaselineStore, ttl: Int, clock: Clock): TieredHubBaselineStore {
    return CachedTieredHubBaselineStore(
        inner = inner,
        cache = newTtlCache<TieredHubBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered hub baselines. Empty fields match all. */
data class TieredHubBaselineQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredHubBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredHubBaseline>): MutableList<TieredHubBaseline> {
        val matched = filter(items) { candidate: TieredHubBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredHubBaselines(store: TieredHubBaselineStore, query: TieredHubBaselineQuery): MutableList<TieredHubBaseline> {
    return query.apply(store.list())
}
