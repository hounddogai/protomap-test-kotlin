package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted hub drafts by code. */
interface WeightedHubDraftStore {
    fun find(code: String): WeightedHubDraft?
    fun save(item: WeightedHubDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedHubDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedHubDraftStore(
    var items: MutableMap<String, WeightedHubDraft>,
    var order: MutableList<String>,
) : WeightedHubDraftStore {
    override fun find(code: String): WeightedHubDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedHubDraft) {
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

    override fun list(): MutableList<WeightedHubDraft> {
        val out = mutableListOf<WeightedHubDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedHubDraftStore(): WeightedHubDraftStore {
    return MemoryWeightedHubDraftStore(mutableMapOf<String, WeightedHubDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedHubDraftStore(
    var inner: WeightedHubDraftStore,
    var cache: TtlCache<WeightedHubDraft>,
    var hits: Int,
    var misses: Int,
) : WeightedHubDraftStore {
    override fun find(code: String): WeightedHubDraft? {
        val hit = (cache.get(code) as WeightedHubDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedHubDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedHubDraft> = inner.list()
}

fun newCachedWeightedHubDraftStore(inner: WeightedHubDraftStore, ttl: Int, clock: Clock): WeightedHubDraftStore {
    return CachedWeightedHubDraftStore(
        inner = inner,
        cache = newTtlCache<WeightedHubDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted hub drafts. Empty fields match all. */
data class WeightedHubDraftQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedHubDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedHubDraft>): MutableList<WeightedHubDraft> {
        val matched = filter(items) { candidate: WeightedHubDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedHubDrafts(store: WeightedHubDraftStore, query: WeightedHubDraftQuery): MutableList<WeightedHubDraft> {
    return query.apply(store.list())
}
