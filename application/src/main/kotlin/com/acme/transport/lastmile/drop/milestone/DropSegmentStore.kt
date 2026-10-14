package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop segments by code. */
interface DropSegmentStore {
    fun find(code: String): DropSegment?
    fun save(item: DropSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropSegmentStore(
    var items: MutableMap<String, DropSegment>,
    var order: MutableList<String>,
) : DropSegmentStore {
    override fun find(code: String): DropSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropSegment) {
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

    override fun list(): MutableList<DropSegment> {
        val out = mutableListOf<DropSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropSegmentStore(): DropSegmentStore {
    return MemoryDropSegmentStore(mutableMapOf<String, DropSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropSegmentStore(
    var inner: DropSegmentStore,
    var cache: TtlCache<DropSegment>,
    var hits: Int,
    var misses: Int,
) : DropSegmentStore {
    override fun find(code: String): DropSegment? {
        val hit = (cache.get(code) as DropSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropSegment> = inner.list()
}

fun newCachedDropSegmentStore(inner: DropSegmentStore, ttl: Int, clock: Clock): DropSegmentStore {
    return CachedDropSegmentStore(
        inner = inner,
        cache = newTtlCache<DropSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop segments. Empty fields match all. */
data class DropSegmentQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropSegment>): MutableList<DropSegment> {
        val matched = filter(items) { candidate: DropSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropSegments(store: DropSegmentStore, query: DropSegmentQuery): MutableList<DropSegment> {
    return query.apply(store.list())
}
