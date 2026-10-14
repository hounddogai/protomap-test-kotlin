package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional cutoff thresholds by code. */
interface RegionalCutoffThresholdStore {
    fun find(code: String): RegionalCutoffThreshold?
    fun save(item: RegionalCutoffThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCutoffThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCutoffThresholdStore(
    var items: MutableMap<String, RegionalCutoffThreshold>,
    var order: MutableList<String>,
) : RegionalCutoffThresholdStore {
    override fun find(code: String): RegionalCutoffThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCutoffThreshold) {
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

    override fun list(): MutableList<RegionalCutoffThreshold> {
        val out = mutableListOf<RegionalCutoffThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCutoffThresholdStore(): RegionalCutoffThresholdStore {
    return MemoryRegionalCutoffThresholdStore(
        mutableMapOf<String, RegionalCutoffThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCutoffThresholdStore(
    var inner: RegionalCutoffThresholdStore,
    var cache: TtlCache<RegionalCutoffThreshold>,
    var hits: Int,
    var misses: Int,
) : RegionalCutoffThresholdStore {
    override fun find(code: String): RegionalCutoffThreshold? {
        val hit = (cache.get(code) as RegionalCutoffThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCutoffThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCutoffThreshold> = inner.list()
}

fun newCachedRegionalCutoffThresholdStore(inner: RegionalCutoffThresholdStore, ttl: Int, clock: Clock): RegionalCutoffThresholdStore {
    return CachedRegionalCutoffThresholdStore(
        inner = inner,
        cache = newTtlCache<RegionalCutoffThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional cutoff thresholds. Empty fields match all. */
data class RegionalCutoffThresholdQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCutoffThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCutoffThreshold>): MutableList<RegionalCutoffThreshold> {
        val matched = filter(items) { candidate: RegionalCutoffThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCutoffThresholds(store: RegionalCutoffThresholdStore, query: RegionalCutoffThresholdQuery): MutableList<RegionalCutoffThreshold> {
    return query.apply(store.list())
}
