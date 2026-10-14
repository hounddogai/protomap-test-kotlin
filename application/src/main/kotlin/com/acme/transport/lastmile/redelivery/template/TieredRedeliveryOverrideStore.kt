package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered redelivery overrides by code. */
interface TieredRedeliveryOverrideStore {
    fun find(code: String): TieredRedeliveryOverride?
    fun save(item: TieredRedeliveryOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRedeliveryOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRedeliveryOverrideStore(
    var items: MutableMap<String, TieredRedeliveryOverride>,
    var order: MutableList<String>,
) : TieredRedeliveryOverrideStore {
    override fun find(code: String): TieredRedeliveryOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRedeliveryOverride) {
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

    override fun list(): MutableList<TieredRedeliveryOverride> {
        val out = mutableListOf<TieredRedeliveryOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRedeliveryOverrideStore(): TieredRedeliveryOverrideStore {
    return MemoryTieredRedeliveryOverrideStore(
        mutableMapOf<String, TieredRedeliveryOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRedeliveryOverrideStore(
    var inner: TieredRedeliveryOverrideStore,
    var cache: TtlCache<TieredRedeliveryOverride>,
    var hits: Int,
    var misses: Int,
) : TieredRedeliveryOverrideStore {
    override fun find(code: String): TieredRedeliveryOverride? {
        val hit = (cache.get(code) as TieredRedeliveryOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRedeliveryOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRedeliveryOverride> = inner.list()
}

fun newCachedTieredRedeliveryOverrideStore(inner: TieredRedeliveryOverrideStore, ttl: Int, clock: Clock): TieredRedeliveryOverrideStore {
    return CachedTieredRedeliveryOverrideStore(
        inner = inner,
        cache = newTtlCache<TieredRedeliveryOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered redelivery overrides. Empty fields match all. */
data class TieredRedeliveryOverrideQuery(
    var source: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRedeliveryOverride): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRedeliveryOverride>): MutableList<TieredRedeliveryOverride> {
        val matched = filter(items) { candidate: TieredRedeliveryOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRedeliveryOverrides(store: TieredRedeliveryOverrideStore, query: TieredRedeliveryOverrideQuery): MutableList<TieredRedeliveryOverride> {
    return query.apply(store.list())
}
