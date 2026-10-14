package com.acme.transport.customs.clearance.manifest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance baselines by code. */
interface ClearanceBaselineStore {
    fun find(code: String): ClearanceBaseline?
    fun save(item: ClearanceBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceBaselineStore(
    var items: MutableMap<String, ClearanceBaseline>,
    var order: MutableList<String>,
) : ClearanceBaselineStore {
    override fun find(code: String): ClearanceBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceBaseline) {
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

    override fun list(): MutableList<ClearanceBaseline> {
        val out = mutableListOf<ClearanceBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceBaselineStore(): ClearanceBaselineStore {
    return MemoryClearanceBaselineStore(mutableMapOf<String, ClearanceBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceBaselineStore(
    var inner: ClearanceBaselineStore,
    var cache: TtlCache<ClearanceBaseline>,
    var hits: Int,
    var misses: Int,
) : ClearanceBaselineStore {
    override fun find(code: String): ClearanceBaseline? {
        val hit = (cache.get(code) as ClearanceBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceBaseline> = inner.list()
}

fun newCachedClearanceBaselineStore(inner: ClearanceBaselineStore, ttl: Int, clock: Clock): ClearanceBaselineStore {
    return CachedClearanceBaselineStore(
        inner = inner,
        cache = newTtlCache<ClearanceBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance baselines. Empty fields match all. */
data class ClearanceBaselineQuery(
    var lane: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceBaseline): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceBaseline>): MutableList<ClearanceBaseline> {
        val matched = filter(items) { candidate: ClearanceBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceBaselines(store: ClearanceBaselineStore, query: ClearanceBaselineQuery): MutableList<ClearanceBaseline> {
    return query.apply(store.list())
}
