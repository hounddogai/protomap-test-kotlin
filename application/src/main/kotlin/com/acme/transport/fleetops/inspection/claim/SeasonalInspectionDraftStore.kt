package com.acme.transport.fleetops.inspection.claim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal inspection drafts by code. */
interface SeasonalInspectionDraftStore {
    fun find(code: String): SeasonalInspectionDraft?
    fun save(item: SeasonalInspectionDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalInspectionDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalInspectionDraftStore(
    var items: MutableMap<String, SeasonalInspectionDraft>,
    var order: MutableList<String>,
) : SeasonalInspectionDraftStore {
    override fun find(code: String): SeasonalInspectionDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalInspectionDraft) {
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

    override fun list(): MutableList<SeasonalInspectionDraft> {
        val out = mutableListOf<SeasonalInspectionDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalInspectionDraftStore(): SeasonalInspectionDraftStore {
    return MemorySeasonalInspectionDraftStore(
        mutableMapOf<String, SeasonalInspectionDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalInspectionDraftStore(
    var inner: SeasonalInspectionDraftStore,
    var cache: TtlCache<SeasonalInspectionDraft>,
    var hits: Int,
    var misses: Int,
) : SeasonalInspectionDraftStore {
    override fun find(code: String): SeasonalInspectionDraft? {
        val hit = (cache.get(code) as SeasonalInspectionDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalInspectionDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalInspectionDraft> = inner.list()
}

fun newCachedSeasonalInspectionDraftStore(inner: SeasonalInspectionDraftStore, ttl: Int, clock: Clock): SeasonalInspectionDraftStore {
    return CachedSeasonalInspectionDraftStore(
        inner = inner,
        cache = newTtlCache<SeasonalInspectionDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal inspection drafts. Empty fields match all. */
data class SeasonalInspectionDraftQuery(
    var depot: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalInspectionDraft): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalInspectionDraft>): MutableList<SeasonalInspectionDraft> {
        val matched = filter(items) { candidate: SeasonalInspectionDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalInspectionDrafts(store: SeasonalInspectionDraftStore, query: SeasonalInspectionDraftQuery): MutableList<SeasonalInspectionDraft> {
    return query.apply(store.list())
}
