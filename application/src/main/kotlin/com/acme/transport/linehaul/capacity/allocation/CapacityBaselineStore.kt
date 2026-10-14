package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity baselines by code. */
interface CapacityBaselineStore {
    fun find(code: String): CapacityBaseline?
    fun save(item: CapacityBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityBaselineStore(
    var items: MutableMap<String, CapacityBaseline>,
    var order: MutableList<String>,
) : CapacityBaselineStore {
    override fun find(code: String): CapacityBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityBaseline) {
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

    override fun list(): MutableList<CapacityBaseline> {
        val out = mutableListOf<CapacityBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityBaselineStore(): CapacityBaselineStore {
    return MemoryCapacityBaselineStore(mutableMapOf<String, CapacityBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityBaselineStore(
    var inner: CapacityBaselineStore,
    var cache: TtlCache<CapacityBaseline>,
    var hits: Int,
    var misses: Int,
) : CapacityBaselineStore {
    override fun find(code: String): CapacityBaseline? {
        val hit = (cache.get(code) as CapacityBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityBaseline> = inner.list()
}

fun newCachedCapacityBaselineStore(inner: CapacityBaselineStore, ttl: Int, clock: Clock): CapacityBaselineStore {
    return CachedCapacityBaselineStore(
        inner = inner,
        cache = newTtlCache<CapacityBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity baselines. Empty fields match all. */
data class CapacityBaselineQuery(
    var region: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityBaseline): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityBaseline>): MutableList<CapacityBaseline> {
        val matched = filter(items) { candidate: CapacityBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityBaselines(store: CapacityBaselineStore, query: CapacityBaselineQuery): MutableList<CapacityBaseline> {
    return query.apply(store.list())
}
