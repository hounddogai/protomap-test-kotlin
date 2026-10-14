package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted availability drafts by code. */
interface WeightedAvailabilityDraftStore {
    fun find(code: String): WeightedAvailabilityDraft?
    fun save(item: WeightedAvailabilityDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAvailabilityDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAvailabilityDraftStore(
    var items: MutableMap<String, WeightedAvailabilityDraft>,
    var order: MutableList<String>,
) : WeightedAvailabilityDraftStore {
    override fun find(code: String): WeightedAvailabilityDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAvailabilityDraft) {
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

    override fun list(): MutableList<WeightedAvailabilityDraft> {
        val out = mutableListOf<WeightedAvailabilityDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAvailabilityDraftStore(): WeightedAvailabilityDraftStore {
    return MemoryWeightedAvailabilityDraftStore(
        mutableMapOf<String, WeightedAvailabilityDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAvailabilityDraftStore(
    var inner: WeightedAvailabilityDraftStore,
    var cache: TtlCache<WeightedAvailabilityDraft>,
    var hits: Int,
    var misses: Int,
) : WeightedAvailabilityDraftStore {
    override fun find(code: String): WeightedAvailabilityDraft? {
        val hit = (cache.get(code) as WeightedAvailabilityDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAvailabilityDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAvailabilityDraft> = inner.list()
}

fun newCachedWeightedAvailabilityDraftStore(inner: WeightedAvailabilityDraftStore, ttl: Int, clock: Clock): WeightedAvailabilityDraftStore {
    return CachedWeightedAvailabilityDraftStore(
        inner = inner,
        cache = newTtlCache<WeightedAvailabilityDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted availability drafts. Empty fields match all. */
data class WeightedAvailabilityDraftQuery(
    var lane: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAvailabilityDraft): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAvailabilityDraft>): MutableList<WeightedAvailabilityDraft> {
        val matched = filter(items) { candidate: WeightedAvailabilityDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAvailabilityDrafts(store: WeightedAvailabilityDraftStore, query: WeightedAvailabilityDraftQuery): MutableList<WeightedAvailabilityDraft> {
    return query.apply(store.list())
}
