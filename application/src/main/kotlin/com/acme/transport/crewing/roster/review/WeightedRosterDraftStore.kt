package com.acme.transport.crewing.roster.review

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted roster drafts by code. */
interface WeightedRosterDraftStore {
    fun find(code: String): WeightedRosterDraft?
    fun save(item: WeightedRosterDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRosterDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRosterDraftStore(
    var items: MutableMap<String, WeightedRosterDraft>,
    var order: MutableList<String>,
) : WeightedRosterDraftStore {
    override fun find(code: String): WeightedRosterDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRosterDraft) {
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

    override fun list(): MutableList<WeightedRosterDraft> {
        val out = mutableListOf<WeightedRosterDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRosterDraftStore(): WeightedRosterDraftStore {
    return MemoryWeightedRosterDraftStore(mutableMapOf<String, WeightedRosterDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRosterDraftStore(
    var inner: WeightedRosterDraftStore,
    var cache: TtlCache<WeightedRosterDraft>,
    var hits: Int,
    var misses: Int,
) : WeightedRosterDraftStore {
    override fun find(code: String): WeightedRosterDraft? {
        val hit = (cache.get(code) as WeightedRosterDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRosterDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRosterDraft> = inner.list()
}

fun newCachedWeightedRosterDraftStore(inner: WeightedRosterDraftStore, ttl: Int, clock: Clock): WeightedRosterDraftStore {
    return CachedWeightedRosterDraftStore(
        inner = inner,
        cache = newTtlCache<WeightedRosterDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted roster drafts. Empty fields match all. */
data class WeightedRosterDraftQuery(
    var category: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRosterDraft): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedRosterDraft>): MutableList<WeightedRosterDraft> {
        val matched = filter(items) { candidate: WeightedRosterDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRosterDrafts(store: WeightedRosterDraftStore, query: WeightedRosterDraftQuery): MutableList<WeightedRosterDraft> {
    return query.apply(store.list())
}
