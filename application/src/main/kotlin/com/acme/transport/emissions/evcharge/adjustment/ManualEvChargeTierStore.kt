package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual ev charge tiers by code. */
interface ManualEvChargeTierStore {
    fun find(code: String): ManualEvChargeTier?
    fun save(item: ManualEvChargeTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualEvChargeTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualEvChargeTierStore(
    var items: MutableMap<String, ManualEvChargeTier>,
    var order: MutableList<String>,
) : ManualEvChargeTierStore {
    override fun find(code: String): ManualEvChargeTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualEvChargeTier) {
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

    override fun list(): MutableList<ManualEvChargeTier> {
        val out = mutableListOf<ManualEvChargeTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualEvChargeTierStore(): ManualEvChargeTierStore {
    return MemoryManualEvChargeTierStore(mutableMapOf<String, ManualEvChargeTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualEvChargeTierStore(
    var inner: ManualEvChargeTierStore,
    var cache: TtlCache<ManualEvChargeTier>,
    var hits: Int,
    var misses: Int,
) : ManualEvChargeTierStore {
    override fun find(code: String): ManualEvChargeTier? {
        val hit = (cache.get(code) as ManualEvChargeTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualEvChargeTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualEvChargeTier> = inner.list()
}

fun newCachedManualEvChargeTierStore(inner: ManualEvChargeTierStore, ttl: Int, clock: Clock): ManualEvChargeTierStore {
    return CachedManualEvChargeTierStore(
        inner = inner,
        cache = newTtlCache<ManualEvChargeTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual ev charge tiers. Empty fields match all. */
data class ManualEvChargeTierQuery(
    var region: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualEvChargeTier): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualEvChargeTier>): MutableList<ManualEvChargeTier> {
        val matched = filter(items) { candidate: ManualEvChargeTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualEvChargeTiers(store: ManualEvChargeTierStore, query: ManualEvChargeTierQuery): MutableList<ManualEvChargeTier> {
    return query.apply(store.list())
}
