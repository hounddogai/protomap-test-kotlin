package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive declaration segments by code. */
interface AdaptiveDeclarationSegmentStore {
    fun find(code: String): AdaptiveDeclarationSegment?
    fun save(item: AdaptiveDeclarationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDeclarationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDeclarationSegmentStore(
    var items: MutableMap<String, AdaptiveDeclarationSegment>,
    var order: MutableList<String>,
) : AdaptiveDeclarationSegmentStore {
    override fun find(code: String): AdaptiveDeclarationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDeclarationSegment) {
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

    override fun list(): MutableList<AdaptiveDeclarationSegment> {
        val out = mutableListOf<AdaptiveDeclarationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDeclarationSegmentStore(): AdaptiveDeclarationSegmentStore {
    return MemoryAdaptiveDeclarationSegmentStore(
        mutableMapOf<String, AdaptiveDeclarationSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDeclarationSegmentStore(
    var inner: AdaptiveDeclarationSegmentStore,
    var cache: TtlCache<AdaptiveDeclarationSegment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDeclarationSegmentStore {
    override fun find(code: String): AdaptiveDeclarationSegment? {
        val hit = (cache.get(code) as AdaptiveDeclarationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDeclarationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDeclarationSegment> = inner.list()
}

fun newCachedAdaptiveDeclarationSegmentStore(inner: AdaptiveDeclarationSegmentStore, ttl: Int, clock: Clock): AdaptiveDeclarationSegmentStore {
    return CachedAdaptiveDeclarationSegmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDeclarationSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive declaration segments. Empty fields match all. */
data class AdaptiveDeclarationSegmentQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDeclarationSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDeclarationSegment>): MutableList<AdaptiveDeclarationSegment> {
        val matched = filter(items) { candidate: AdaptiveDeclarationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDeclarationSegments(store: AdaptiveDeclarationSegmentStore, query: AdaptiveDeclarationSegmentQuery): MutableList<AdaptiveDeclarationSegment> {
    return query.apply(store.list())
}
