package com.acme.transport.customs.hold.audit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold estimates by code. */
interface HoldEstimateStore {
    fun find(code: String): HoldEstimate?
    fun save(item: HoldEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldEstimateStore(
    var items: MutableMap<String, HoldEstimate>,
    var order: MutableList<String>,
) : HoldEstimateStore {
    override fun find(code: String): HoldEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldEstimate) {
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

    override fun list(): MutableList<HoldEstimate> {
        val out = mutableListOf<HoldEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldEstimateStore(): HoldEstimateStore {
    return MemoryHoldEstimateStore(mutableMapOf<String, HoldEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldEstimateStore(
    var inner: HoldEstimateStore,
    var cache: TtlCache<HoldEstimate>,
    var hits: Int,
    var misses: Int,
) : HoldEstimateStore {
    override fun find(code: String): HoldEstimate? {
        val hit = (cache.get(code) as HoldEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldEstimate> = inner.list()
}

fun newCachedHoldEstimateStore(inner: HoldEstimateStore, ttl: Int, clock: Clock): HoldEstimateStore {
    return CachedHoldEstimateStore(
        inner = inner,
        cache = newTtlCache<HoldEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold estimates. Empty fields match all. */
data class HoldEstimateQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldEstimate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldEstimate>): MutableList<HoldEstimate> {
        val matched = filter(items) { candidate: HoldEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldEstimates(store: HoldEstimateStore, query: HoldEstimateQuery): MutableList<HoldEstimate> {
    return query.apply(store.list())
}
