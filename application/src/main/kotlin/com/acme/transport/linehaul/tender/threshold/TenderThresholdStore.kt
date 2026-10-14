package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender thresholds by code. */
interface TenderThresholdStore {
    fun find(code: String): TenderThreshold?
    fun save(item: TenderThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderThresholdStore(
    var items: MutableMap<String, TenderThreshold>,
    var order: MutableList<String>,
) : TenderThresholdStore {
    override fun find(code: String): TenderThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderThreshold) {
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

    override fun list(): MutableList<TenderThreshold> {
        val out = mutableListOf<TenderThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderThresholdStore(): TenderThresholdStore {
    return MemoryTenderThresholdStore(mutableMapOf<String, TenderThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderThresholdStore(
    var inner: TenderThresholdStore,
    var cache: TtlCache<TenderThreshold>,
    var hits: Int,
    var misses: Int,
) : TenderThresholdStore {
    override fun find(code: String): TenderThreshold? {
        val hit = (cache.get(code) as TenderThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderThreshold> = inner.list()
}

fun newCachedTenderThresholdStore(inner: TenderThresholdStore, ttl: Int, clock: Clock): TenderThresholdStore {
    return CachedTenderThresholdStore(
        inner = inner,
        cache = newTtlCache<TenderThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender thresholds. Empty fields match all. */
data class TenderThresholdQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderThreshold): Boolean {
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

    fun apply(items: MutableList<TenderThreshold>): MutableList<TenderThreshold> {
        val matched = filter(items) { candidate: TenderThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderThresholds(store: TenderThresholdStore, query: TenderThresholdQuery): MutableList<TenderThreshold> {
    return query.apply(store.list())
}
