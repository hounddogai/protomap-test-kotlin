package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul baselines by code. */
interface BackhaulBaselineStore {
    fun find(code: String): BackhaulBaseline?
    fun save(item: BackhaulBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulBaselineStore(
    var items: MutableMap<String, BackhaulBaseline>,
    var order: MutableList<String>,
) : BackhaulBaselineStore {
    override fun find(code: String): BackhaulBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulBaseline) {
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

    override fun list(): MutableList<BackhaulBaseline> {
        val out = mutableListOf<BackhaulBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulBaselineStore(): BackhaulBaselineStore {
    return MemoryBackhaulBaselineStore(mutableMapOf<String, BackhaulBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulBaselineStore(
    var inner: BackhaulBaselineStore,
    var cache: TtlCache<BackhaulBaseline>,
    var hits: Int,
    var misses: Int,
) : BackhaulBaselineStore {
    override fun find(code: String): BackhaulBaseline? {
        val hit = (cache.get(code) as BackhaulBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulBaseline> = inner.list()
}

fun newCachedBackhaulBaselineStore(inner: BackhaulBaselineStore, ttl: Int, clock: Clock): BackhaulBaselineStore {
    return CachedBackhaulBaselineStore(
        inner = inner,
        cache = newTtlCache<BackhaulBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul baselines. Empty fields match all. */
data class BackhaulBaselineQuery(
    var country: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulBaseline): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulBaseline>): MutableList<BackhaulBaseline> {
        val matched = filter(items) { candidate: BackhaulBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulBaselines(store: BackhaulBaselineStore, query: BackhaulBaselineQuery): MutableList<BackhaulBaseline> {
    return query.apply(store.list())
}
