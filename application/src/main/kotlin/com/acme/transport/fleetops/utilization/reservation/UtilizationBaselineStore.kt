package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization baselines by code. */
interface UtilizationBaselineStore {
    fun find(code: String): UtilizationBaseline?
    fun save(item: UtilizationBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationBaselineStore(
    var items: MutableMap<String, UtilizationBaseline>,
    var order: MutableList<String>,
) : UtilizationBaselineStore {
    override fun find(code: String): UtilizationBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationBaseline) {
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

    override fun list(): MutableList<UtilizationBaseline> {
        val out = mutableListOf<UtilizationBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationBaselineStore(): UtilizationBaselineStore {
    return MemoryUtilizationBaselineStore(mutableMapOf<String, UtilizationBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationBaselineStore(
    var inner: UtilizationBaselineStore,
    var cache: TtlCache<UtilizationBaseline>,
    var hits: Int,
    var misses: Int,
) : UtilizationBaselineStore {
    override fun find(code: String): UtilizationBaseline? {
        val hit = (cache.get(code) as UtilizationBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationBaseline> = inner.list()
}

fun newCachedUtilizationBaselineStore(inner: UtilizationBaselineStore, ttl: Int, clock: Clock): UtilizationBaselineStore {
    return CachedUtilizationBaselineStore(
        inner = inner,
        cache = newTtlCache<UtilizationBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization baselines. Empty fields match all. */
data class UtilizationBaselineQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationBaseline): Boolean {
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

    fun apply(items: MutableList<UtilizationBaseline>): MutableList<UtilizationBaseline> {
        val matched = filter(items) { candidate: UtilizationBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationBaselines(store: UtilizationBaselineStore, query: UtilizationBaselineQuery): MutableList<UtilizationBaseline> {
    return query.apply(store.list())
}
