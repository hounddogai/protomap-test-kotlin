package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge segments by code. */
interface EvChargeSegmentStore {
    fun find(code: String): EvChargeSegment?
    fun save(item: EvChargeSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeSegmentStore(
    var items: MutableMap<String, EvChargeSegment>,
    var order: MutableList<String>,
) : EvChargeSegmentStore {
    override fun find(code: String): EvChargeSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeSegment) {
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

    override fun list(): MutableList<EvChargeSegment> {
        val out = mutableListOf<EvChargeSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeSegmentStore(): EvChargeSegmentStore {
    return MemoryEvChargeSegmentStore(mutableMapOf<String, EvChargeSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeSegmentStore(
    var inner: EvChargeSegmentStore,
    var cache: TtlCache<EvChargeSegment>,
    var hits: Int,
    var misses: Int,
) : EvChargeSegmentStore {
    override fun find(code: String): EvChargeSegment? {
        val hit = (cache.get(code) as EvChargeSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeSegment> = inner.list()
}

fun newCachedEvChargeSegmentStore(inner: EvChargeSegmentStore, ttl: Int, clock: Clock): EvChargeSegmentStore {
    return CachedEvChargeSegmentStore(
        inner = inner,
        cache = newTtlCache<EvChargeSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge segments. Empty fields match all. */
data class EvChargeSegmentQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeSegment>): MutableList<EvChargeSegment> {
        val matched = filter(items) { candidate: EvChargeSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeSegments(store: EvChargeSegmentStore, query: EvChargeSegmentQuery): MutableList<EvChargeSegment> {
    return query.apply(store.list())
}
