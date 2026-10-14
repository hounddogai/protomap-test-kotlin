package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback inspection drafts by code. */
interface FallbackInspectionDraftStore {
    fun find(code: String): FallbackInspectionDraft?
    fun save(item: FallbackInspectionDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackInspectionDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackInspectionDraftStore(
    var items: MutableMap<String, FallbackInspectionDraft>,
    var order: MutableList<String>,
) : FallbackInspectionDraftStore {
    override fun find(code: String): FallbackInspectionDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackInspectionDraft) {
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

    override fun list(): MutableList<FallbackInspectionDraft> {
        val out = mutableListOf<FallbackInspectionDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackInspectionDraftStore(): FallbackInspectionDraftStore {
    return MemoryFallbackInspectionDraftStore(
        mutableMapOf<String, FallbackInspectionDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackInspectionDraftStore(
    var inner: FallbackInspectionDraftStore,
    var cache: TtlCache<FallbackInspectionDraft>,
    var hits: Int,
    var misses: Int,
) : FallbackInspectionDraftStore {
    override fun find(code: String): FallbackInspectionDraft? {
        val hit = (cache.get(code) as FallbackInspectionDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackInspectionDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackInspectionDraft> = inner.list()
}

fun newCachedFallbackInspectionDraftStore(inner: FallbackInspectionDraftStore, ttl: Int, clock: Clock): FallbackInspectionDraftStore {
    return CachedFallbackInspectionDraftStore(
        inner = inner,
        cache = newTtlCache<FallbackInspectionDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback inspection drafts. Empty fields match all. */
data class FallbackInspectionDraftQuery(
    var country: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackInspectionDraft): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackInspectionDraft>): MutableList<FallbackInspectionDraft> {
        val matched = filter(items) { candidate: FallbackInspectionDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackInspectionDrafts(store: FallbackInspectionDraftStore, query: FallbackInspectionDraftQuery): MutableList<FallbackInspectionDraft> {
    return query.apply(store.list())
}
