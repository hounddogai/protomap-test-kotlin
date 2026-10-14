package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered parcel baselines by code. */
interface TieredParcelBaselineStore {
    fun find(code: String): TieredParcelBaseline?
    fun save(item: TieredParcelBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredParcelBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredParcelBaselineStore(
    var items: MutableMap<String, TieredParcelBaseline>,
    var order: MutableList<String>,
) : TieredParcelBaselineStore {
    override fun find(code: String): TieredParcelBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredParcelBaseline) {
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

    override fun list(): MutableList<TieredParcelBaseline> {
        val out = mutableListOf<TieredParcelBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredParcelBaselineStore(): TieredParcelBaselineStore {
    return MemoryTieredParcelBaselineStore(
        mutableMapOf<String, TieredParcelBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredParcelBaselineStore(
    var inner: TieredParcelBaselineStore,
    var cache: TtlCache<TieredParcelBaseline>,
    var hits: Int,
    var misses: Int,
) : TieredParcelBaselineStore {
    override fun find(code: String): TieredParcelBaseline? {
        val hit = (cache.get(code) as TieredParcelBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredParcelBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredParcelBaseline> = inner.list()
}

fun newCachedTieredParcelBaselineStore(inner: TieredParcelBaselineStore, ttl: Int, clock: Clock): TieredParcelBaselineStore {
    return CachedTieredParcelBaselineStore(
        inner = inner,
        cache = newTtlCache<TieredParcelBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered parcel baselines. Empty fields match all. */
data class TieredParcelBaselineQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredParcelBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredParcelBaseline>): MutableList<TieredParcelBaseline> {
        val matched = filter(items) { candidate: TieredParcelBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredParcelBaselines(store: TieredParcelBaselineStore, query: TieredParcelBaselineQuery): MutableList<TieredParcelBaseline> {
    return query.apply(store.list())
}
