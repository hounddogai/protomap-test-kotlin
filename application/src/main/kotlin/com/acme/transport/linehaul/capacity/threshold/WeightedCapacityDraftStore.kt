package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted capacity drafts by code. */
interface WeightedCapacityDraftStore {
    fun find(code: String): WeightedCapacityDraft?
    fun save(item: WeightedCapacityDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCapacityDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCapacityDraftStore(
    var items: MutableMap<String, WeightedCapacityDraft>,
    var order: MutableList<String>,
) : WeightedCapacityDraftStore {
    override fun find(code: String): WeightedCapacityDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCapacityDraft) {
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

    override fun list(): MutableList<WeightedCapacityDraft> {
        val out = mutableListOf<WeightedCapacityDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCapacityDraftStore(): WeightedCapacityDraftStore {
    return MemoryWeightedCapacityDraftStore(
        mutableMapOf<String, WeightedCapacityDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCapacityDraftStore(
    var inner: WeightedCapacityDraftStore,
    var cache: TtlCache<WeightedCapacityDraft>,
    var hits: Int,
    var misses: Int,
) : WeightedCapacityDraftStore {
    override fun find(code: String): WeightedCapacityDraft? {
        val hit = (cache.get(code) as WeightedCapacityDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCapacityDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCapacityDraft> = inner.list()
}

fun newCachedWeightedCapacityDraftStore(inner: WeightedCapacityDraftStore, ttl: Int, clock: Clock): WeightedCapacityDraftStore {
    return CachedWeightedCapacityDraftStore(
        inner = inner,
        cache = newTtlCache<WeightedCapacityDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted capacity drafts. Empty fields match all. */
data class WeightedCapacityDraftQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCapacityDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedCapacityDraft>): MutableList<WeightedCapacityDraft> {
        val matched = filter(items) { candidate: WeightedCapacityDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCapacityDrafts(store: WeightedCapacityDraftStore, query: WeightedCapacityDraftQuery): MutableList<WeightedCapacityDraft> {
    return query.apply(store.list())
}
