package com.acme.transport.emissions.idling.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default idling segments by code. */
interface DefaultIdlingSegmentStore {
    fun find(code: String): DefaultIdlingSegment?
    fun save(item: DefaultIdlingSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultIdlingSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultIdlingSegmentStore(
    var items: MutableMap<String, DefaultIdlingSegment>,
    var order: MutableList<String>,
) : DefaultIdlingSegmentStore {
    override fun find(code: String): DefaultIdlingSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultIdlingSegment) {
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

    override fun list(): MutableList<DefaultIdlingSegment> {
        val out = mutableListOf<DefaultIdlingSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultIdlingSegmentStore(): DefaultIdlingSegmentStore {
    return MemoryDefaultIdlingSegmentStore(
        mutableMapOf<String, DefaultIdlingSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultIdlingSegmentStore(
    var inner: DefaultIdlingSegmentStore,
    var cache: TtlCache<DefaultIdlingSegment>,
    var hits: Int,
    var misses: Int,
) : DefaultIdlingSegmentStore {
    override fun find(code: String): DefaultIdlingSegment? {
        val hit = (cache.get(code) as DefaultIdlingSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultIdlingSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultIdlingSegment> = inner.list()
}

fun newCachedDefaultIdlingSegmentStore(inner: DefaultIdlingSegmentStore, ttl: Int, clock: Clock): DefaultIdlingSegmentStore {
    return CachedDefaultIdlingSegmentStore(
        inner = inner,
        cache = newTtlCache<DefaultIdlingSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default idling segments. Empty fields match all. */
data class DefaultIdlingSegmentQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultIdlingSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<DefaultIdlingSegment>): MutableList<DefaultIdlingSegment> {
        val matched = filter(items) { candidate: DefaultIdlingSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultIdlingSegments(store: DefaultIdlingSegmentStore, query: DefaultIdlingSegmentQuery): MutableList<DefaultIdlingSegment> {
    return query.apply(store.list())
}
