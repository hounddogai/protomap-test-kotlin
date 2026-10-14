package com.acme.transport.crewing.availability.segment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary availability baselines by code. */
interface PrimaryAvailabilityBaselineStore {
    fun find(code: String): PrimaryAvailabilityBaseline?
    fun save(item: PrimaryAvailabilityBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAvailabilityBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAvailabilityBaselineStore(
    var items: MutableMap<String, PrimaryAvailabilityBaseline>,
    var order: MutableList<String>,
) : PrimaryAvailabilityBaselineStore {
    override fun find(code: String): PrimaryAvailabilityBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAvailabilityBaseline) {
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

    override fun list(): MutableList<PrimaryAvailabilityBaseline> {
        val out = mutableListOf<PrimaryAvailabilityBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAvailabilityBaselineStore(): PrimaryAvailabilityBaselineStore {
    return MemoryPrimaryAvailabilityBaselineStore(
        mutableMapOf<String, PrimaryAvailabilityBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAvailabilityBaselineStore(
    var inner: PrimaryAvailabilityBaselineStore,
    var cache: TtlCache<PrimaryAvailabilityBaseline>,
    var hits: Int,
    var misses: Int,
) : PrimaryAvailabilityBaselineStore {
    override fun find(code: String): PrimaryAvailabilityBaseline? {
        val hit = (cache.get(code) as PrimaryAvailabilityBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAvailabilityBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAvailabilityBaseline> = inner.list()
}

fun newCachedPrimaryAvailabilityBaselineStore(inner: PrimaryAvailabilityBaselineStore, ttl: Int, clock: Clock): PrimaryAvailabilityBaselineStore {
    return CachedPrimaryAvailabilityBaselineStore(
        inner = inner,
        cache = newTtlCache<PrimaryAvailabilityBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary availability baselines. Empty fields match all. */
data class PrimaryAvailabilityBaselineQuery(
    var unit: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAvailabilityBaseline): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryAvailabilityBaseline>): MutableList<PrimaryAvailabilityBaseline> {
        val matched = filter(items) { candidate: PrimaryAvailabilityBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAvailabilityBaselines(store: PrimaryAvailabilityBaselineStore, query: PrimaryAvailabilityBaselineQuery): MutableList<PrimaryAvailabilityBaseline> {
    return query.apply(store.list())
}
