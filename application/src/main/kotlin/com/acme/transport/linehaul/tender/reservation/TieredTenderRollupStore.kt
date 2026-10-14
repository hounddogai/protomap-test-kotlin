package com.acme.transport.linehaul.tender.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered tender rollups by code. */
interface TieredTenderRollupStore {
    fun find(code: String): TieredTenderRollup?
    fun save(item: TieredTenderRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTenderRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTenderRollupStore(
    var items: MutableMap<String, TieredTenderRollup>,
    var order: MutableList<String>,
) : TieredTenderRollupStore {
    override fun find(code: String): TieredTenderRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTenderRollup) {
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

    override fun list(): MutableList<TieredTenderRollup> {
        val out = mutableListOf<TieredTenderRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTenderRollupStore(): TieredTenderRollupStore {
    return MemoryTieredTenderRollupStore(mutableMapOf<String, TieredTenderRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTenderRollupStore(
    var inner: TieredTenderRollupStore,
    var cache: TtlCache<TieredTenderRollup>,
    var hits: Int,
    var misses: Int,
) : TieredTenderRollupStore {
    override fun find(code: String): TieredTenderRollup? {
        val hit = (cache.get(code) as TieredTenderRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTenderRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTenderRollup> = inner.list()
}

fun newCachedTieredTenderRollupStore(inner: TieredTenderRollupStore, ttl: Int, clock: Clock): TieredTenderRollupStore {
    return CachedTieredTenderRollupStore(
        inner = inner,
        cache = newTtlCache<TieredTenderRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered tender rollups. Empty fields match all. */
data class TieredTenderRollupQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTenderRollup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TieredTenderRollup>): MutableList<TieredTenderRollup> {
        val matched = filter(items) { candidate: TieredTenderRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTenderRollups(store: TieredTenderRollupStore, query: TieredTenderRollupQuery): MutableList<TieredTenderRollup> {
    return query.apply(store.list())
}
