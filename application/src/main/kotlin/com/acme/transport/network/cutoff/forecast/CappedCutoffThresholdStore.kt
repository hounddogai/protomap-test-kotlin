package com.acme.transport.network.cutoff.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped cutoff thresholds by code. */
interface CappedCutoffThresholdStore {
    fun find(code: String): CappedCutoffThreshold?
    fun save(item: CappedCutoffThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCutoffThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCutoffThresholdStore(
    var items: MutableMap<String, CappedCutoffThreshold>,
    var order: MutableList<String>,
) : CappedCutoffThresholdStore {
    override fun find(code: String): CappedCutoffThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCutoffThreshold) {
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

    override fun list(): MutableList<CappedCutoffThreshold> {
        val out = mutableListOf<CappedCutoffThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCutoffThresholdStore(): CappedCutoffThresholdStore {
    return MemoryCappedCutoffThresholdStore(
        mutableMapOf<String, CappedCutoffThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCutoffThresholdStore(
    var inner: CappedCutoffThresholdStore,
    var cache: TtlCache<CappedCutoffThreshold>,
    var hits: Int,
    var misses: Int,
) : CappedCutoffThresholdStore {
    override fun find(code: String): CappedCutoffThreshold? {
        val hit = (cache.get(code) as CappedCutoffThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCutoffThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCutoffThreshold> = inner.list()
}

fun newCachedCappedCutoffThresholdStore(inner: CappedCutoffThresholdStore, ttl: Int, clock: Clock): CappedCutoffThresholdStore {
    return CachedCappedCutoffThresholdStore(
        inner = inner,
        cache = newTtlCache<CappedCutoffThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped cutoff thresholds. Empty fields match all. */
data class CappedCutoffThresholdQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCutoffThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCutoffThreshold>): MutableList<CappedCutoffThreshold> {
        val matched = filter(items) { candidate: CappedCutoffThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCutoffThresholds(store: CappedCutoffThresholdStore, query: CappedCutoffThresholdQuery): MutableList<CappedCutoffThreshold> {
    return query.apply(store.list())
}
