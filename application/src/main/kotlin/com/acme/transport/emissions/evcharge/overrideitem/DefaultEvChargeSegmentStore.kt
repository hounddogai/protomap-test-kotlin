package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default ev charge segments by code. */
interface DefaultEvChargeSegmentStore {
    fun find(code: String): DefaultEvChargeSegment?
    fun save(item: DefaultEvChargeSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultEvChargeSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultEvChargeSegmentStore(
    var items: MutableMap<String, DefaultEvChargeSegment>,
    var order: MutableList<String>,
) : DefaultEvChargeSegmentStore {
    override fun find(code: String): DefaultEvChargeSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultEvChargeSegment) {
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

    override fun list(): MutableList<DefaultEvChargeSegment> {
        val out = mutableListOf<DefaultEvChargeSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultEvChargeSegmentStore(): DefaultEvChargeSegmentStore {
    return MemoryDefaultEvChargeSegmentStore(
        mutableMapOf<String, DefaultEvChargeSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultEvChargeSegmentStore(
    var inner: DefaultEvChargeSegmentStore,
    var cache: TtlCache<DefaultEvChargeSegment>,
    var hits: Int,
    var misses: Int,
) : DefaultEvChargeSegmentStore {
    override fun find(code: String): DefaultEvChargeSegment? {
        val hit = (cache.get(code) as DefaultEvChargeSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultEvChargeSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultEvChargeSegment> = inner.list()
}

fun newCachedDefaultEvChargeSegmentStore(inner: DefaultEvChargeSegmentStore, ttl: Int, clock: Clock): DefaultEvChargeSegmentStore {
    return CachedDefaultEvChargeSegmentStore(
        inner = inner,
        cache = newTtlCache<DefaultEvChargeSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default ev charge segments. Empty fields match all. */
data class DefaultEvChargeSegmentQuery(
    var category: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultEvChargeSegment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultEvChargeSegment>): MutableList<DefaultEvChargeSegment> {
        val matched = filter(items) { candidate: DefaultEvChargeSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultEvChargeSegments(store: DefaultEvChargeSegmentStore, query: DefaultEvChargeSegmentQuery): MutableList<DefaultEvChargeSegment> {
    return query.apply(store.list())
}
