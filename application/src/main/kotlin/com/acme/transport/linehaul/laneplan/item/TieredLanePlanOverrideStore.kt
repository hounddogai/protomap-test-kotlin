package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered lane plan overrides by code. */
interface TieredLanePlanOverrideStore {
    fun find(code: String): TieredLanePlanOverride?
    fun save(item: TieredLanePlanOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredLanePlanOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredLanePlanOverrideStore(
    var items: MutableMap<String, TieredLanePlanOverride>,
    var order: MutableList<String>,
) : TieredLanePlanOverrideStore {
    override fun find(code: String): TieredLanePlanOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredLanePlanOverride) {
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

    override fun list(): MutableList<TieredLanePlanOverride> {
        val out = mutableListOf<TieredLanePlanOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredLanePlanOverrideStore(): TieredLanePlanOverrideStore {
    return MemoryTieredLanePlanOverrideStore(
        mutableMapOf<String, TieredLanePlanOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredLanePlanOverrideStore(
    var inner: TieredLanePlanOverrideStore,
    var cache: TtlCache<TieredLanePlanOverride>,
    var hits: Int,
    var misses: Int,
) : TieredLanePlanOverrideStore {
    override fun find(code: String): TieredLanePlanOverride? {
        val hit = (cache.get(code) as TieredLanePlanOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredLanePlanOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredLanePlanOverride> = inner.list()
}

fun newCachedTieredLanePlanOverrideStore(inner: TieredLanePlanOverrideStore, ttl: Int, clock: Clock): TieredLanePlanOverrideStore {
    return CachedTieredLanePlanOverrideStore(
        inner = inner,
        cache = newTtlCache<TieredLanePlanOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered lane plan overrides. Empty fields match all. */
data class TieredLanePlanOverrideQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredLanePlanOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredLanePlanOverride>): MutableList<TieredLanePlanOverride> {
        val matched = filter(items) { candidate: TieredLanePlanOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredLanePlanOverrides(store: TieredLanePlanOverrideStore, query: TieredLanePlanOverrideQuery): MutableList<TieredLanePlanOverride> {
    return query.apply(store.list())
}
