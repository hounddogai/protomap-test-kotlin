package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell thresholds by code. */
interface DwellThresholdStore {
    fun find(code: String): DwellThreshold?
    fun save(item: DwellThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellThresholdStore(
    var items: MutableMap<String, DwellThreshold>,
    var order: MutableList<String>,
) : DwellThresholdStore {
    override fun find(code: String): DwellThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellThreshold) {
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

    override fun list(): MutableList<DwellThreshold> {
        val out = mutableListOf<DwellThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellThresholdStore(): DwellThresholdStore {
    return MemoryDwellThresholdStore(mutableMapOf<String, DwellThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellThresholdStore(
    var inner: DwellThresholdStore,
    var cache: TtlCache<DwellThreshold>,
    var hits: Int,
    var misses: Int,
) : DwellThresholdStore {
    override fun find(code: String): DwellThreshold? {
        val hit = (cache.get(code) as DwellThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellThreshold> = inner.list()
}

fun newCachedDwellThresholdStore(inner: DwellThresholdStore, ttl: Int, clock: Clock): DwellThresholdStore {
    return CachedDwellThresholdStore(
        inner = inner,
        cache = newTtlCache<DwellThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell thresholds. Empty fields match all. */
data class DwellThresholdQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellThreshold>): MutableList<DwellThreshold> {
        val matched = filter(items) { candidate: DwellThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellThresholds(store: DwellThresholdStore, query: DwellThresholdQuery): MutableList<DwellThreshold> {
    return query.apply(store.list())
}
