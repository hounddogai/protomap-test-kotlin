package com.acme.transport.emissions.evcharge.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge thresholds by code. */
interface EvChargeThresholdStore {
    fun find(code: String): EvChargeThreshold?
    fun save(item: EvChargeThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeThresholdStore(
    var items: MutableMap<String, EvChargeThreshold>,
    var order: MutableList<String>,
) : EvChargeThresholdStore {
    override fun find(code: String): EvChargeThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeThreshold) {
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

    override fun list(): MutableList<EvChargeThreshold> {
        val out = mutableListOf<EvChargeThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeThresholdStore(): EvChargeThresholdStore {
    return MemoryEvChargeThresholdStore(mutableMapOf<String, EvChargeThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeThresholdStore(
    var inner: EvChargeThresholdStore,
    var cache: TtlCache<EvChargeThreshold>,
    var hits: Int,
    var misses: Int,
) : EvChargeThresholdStore {
    override fun find(code: String): EvChargeThreshold? {
        val hit = (cache.get(code) as EvChargeThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeThreshold> = inner.list()
}

fun newCachedEvChargeThresholdStore(inner: EvChargeThresholdStore, ttl: Int, clock: Clock): EvChargeThresholdStore {
    return CachedEvChargeThresholdStore(
        inner = inner,
        cache = newTtlCache<EvChargeThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge thresholds. Empty fields match all. */
data class EvChargeThresholdQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeThreshold>): MutableList<EvChargeThreshold> {
        val matched = filter(items) { candidate: EvChargeThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeThresholds(store: EvChargeThresholdStore, query: EvChargeThresholdQuery): MutableList<EvChargeThreshold> {
    return query.apply(store.list())
}
