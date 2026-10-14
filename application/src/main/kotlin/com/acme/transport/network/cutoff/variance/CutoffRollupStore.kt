package com.acme.transport.network.cutoff.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff rollups by code. */
interface CutoffRollupStore {
    fun find(code: String): CutoffRollup?
    fun save(item: CutoffRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffRollupStore(
    var items: MutableMap<String, CutoffRollup>,
    var order: MutableList<String>,
) : CutoffRollupStore {
    override fun find(code: String): CutoffRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffRollup) {
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

    override fun list(): MutableList<CutoffRollup> {
        val out = mutableListOf<CutoffRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffRollupStore(): CutoffRollupStore {
    return MemoryCutoffRollupStore(mutableMapOf<String, CutoffRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffRollupStore(
    var inner: CutoffRollupStore,
    var cache: TtlCache<CutoffRollup>,
    var hits: Int,
    var misses: Int,
) : CutoffRollupStore {
    override fun find(code: String): CutoffRollup? {
        val hit = (cache.get(code) as CutoffRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffRollup> = inner.list()
}

fun newCachedCutoffRollupStore(inner: CutoffRollupStore, ttl: Int, clock: Clock): CutoffRollupStore {
    return CachedCutoffRollupStore(
        inner = inner,
        cache = newTtlCache<CutoffRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff rollups. Empty fields match all. */
data class CutoffRollupQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffRollup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffRollup>): MutableList<CutoffRollup> {
        val matched = filter(items) { candidate: CutoffRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffRollups(store: CutoffRollupStore, query: CutoffRollupQuery): MutableList<CutoffRollup> {
    return query.apply(store.list())
}
