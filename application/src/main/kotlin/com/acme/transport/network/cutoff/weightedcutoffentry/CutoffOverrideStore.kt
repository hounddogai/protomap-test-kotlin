package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff overrides by code. */
interface CutoffOverrideStore {
    fun find(code: String): CutoffOverride?
    fun save(item: CutoffOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffOverrideStore(
    var items: MutableMap<String, CutoffOverride>,
    var order: MutableList<String>,
) : CutoffOverrideStore {
    override fun find(code: String): CutoffOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffOverride) {
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

    override fun list(): MutableList<CutoffOverride> {
        val out = mutableListOf<CutoffOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffOverrideStore(): CutoffOverrideStore {
    return MemoryCutoffOverrideStore(mutableMapOf<String, CutoffOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffOverrideStore(
    var inner: CutoffOverrideStore,
    var cache: TtlCache<CutoffOverride>,
    var hits: Int,
    var misses: Int,
) : CutoffOverrideStore {
    override fun find(code: String): CutoffOverride? {
        val hit = (cache.get(code) as CutoffOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffOverride> = inner.list()
}

fun newCachedCutoffOverrideStore(inner: CutoffOverrideStore, ttl: Int, clock: Clock): CutoffOverrideStore {
    return CachedCutoffOverrideStore(
        inner = inner,
        cache = newTtlCache<CutoffOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff overrides. Empty fields match all. */
data class CutoffOverrideQuery(
    var unit: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffOverride): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffOverride>): MutableList<CutoffOverride> {
        val matched = filter(items) { candidate: CutoffOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffOverrides(store: CutoffOverrideStore, query: CutoffOverrideQuery): MutableList<CutoffOverride> {
    return query.apply(store.list())
}
