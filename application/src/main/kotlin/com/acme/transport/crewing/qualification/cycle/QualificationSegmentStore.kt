package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification segments by code. */
interface QualificationSegmentStore {
    fun find(code: String): QualificationSegment?
    fun save(item: QualificationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationSegmentStore(
    var items: MutableMap<String, QualificationSegment>,
    var order: MutableList<String>,
) : QualificationSegmentStore {
    override fun find(code: String): QualificationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationSegment) {
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

    override fun list(): MutableList<QualificationSegment> {
        val out = mutableListOf<QualificationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationSegmentStore(): QualificationSegmentStore {
    return MemoryQualificationSegmentStore(
        mutableMapOf<String, QualificationSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationSegmentStore(
    var inner: QualificationSegmentStore,
    var cache: TtlCache<QualificationSegment>,
    var hits: Int,
    var misses: Int,
) : QualificationSegmentStore {
    override fun find(code: String): QualificationSegment? {
        val hit = (cache.get(code) as QualificationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationSegment> = inner.list()
}

fun newCachedQualificationSegmentStore(inner: QualificationSegmentStore, ttl: Int, clock: Clock): QualificationSegmentStore {
    return CachedQualificationSegmentStore(
        inner = inner,
        cache = newTtlCache<QualificationSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification segments. Empty fields match all. */
data class QualificationSegmentQuery(
    var lane: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationSegment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<QualificationSegment>): MutableList<QualificationSegment> {
        val matched = filter(items) { candidate: QualificationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationSegments(store: QualificationSegmentStore, query: QualificationSegmentQuery): MutableList<QualificationSegment> {
    return query.apply(store.list())
}
