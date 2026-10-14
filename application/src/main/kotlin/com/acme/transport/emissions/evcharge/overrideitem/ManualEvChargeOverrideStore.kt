package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual ev charge overrides by code. */
interface ManualEvChargeOverrideStore {
    fun find(code: String): ManualEvChargeOverride?
    fun save(item: ManualEvChargeOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualEvChargeOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualEvChargeOverrideStore(
    var items: MutableMap<String, ManualEvChargeOverride>,
    var order: MutableList<String>,
) : ManualEvChargeOverrideStore {
    override fun find(code: String): ManualEvChargeOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualEvChargeOverride) {
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

    override fun list(): MutableList<ManualEvChargeOverride> {
        val out = mutableListOf<ManualEvChargeOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualEvChargeOverrideStore(): ManualEvChargeOverrideStore {
    return MemoryManualEvChargeOverrideStore(
        mutableMapOf<String, ManualEvChargeOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualEvChargeOverrideStore(
    var inner: ManualEvChargeOverrideStore,
    var cache: TtlCache<ManualEvChargeOverride>,
    var hits: Int,
    var misses: Int,
) : ManualEvChargeOverrideStore {
    override fun find(code: String): ManualEvChargeOverride? {
        val hit = (cache.get(code) as ManualEvChargeOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualEvChargeOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualEvChargeOverride> = inner.list()
}

fun newCachedManualEvChargeOverrideStore(inner: ManualEvChargeOverrideStore, ttl: Int, clock: Clock): ManualEvChargeOverrideStore {
    return CachedManualEvChargeOverrideStore(
        inner = inner,
        cache = newTtlCache<ManualEvChargeOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual ev charge overrides. Empty fields match all. */
data class ManualEvChargeOverrideQuery(
    var category: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualEvChargeOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualEvChargeOverride>): MutableList<ManualEvChargeOverride> {
        val matched = filter(items) { candidate: ManualEvChargeOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualEvChargeOverrides(store: ManualEvChargeOverrideStore, query: ManualEvChargeOverrideQuery): MutableList<ManualEvChargeOverride> {
    return query.apply(store.list())
}
