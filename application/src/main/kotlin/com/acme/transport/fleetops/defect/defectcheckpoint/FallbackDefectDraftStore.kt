package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback defect drafts by code. */
interface FallbackDefectDraftStore {
    fun find(code: String): FallbackDefectDraft?
    fun save(item: FallbackDefectDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDefectDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDefectDraftStore(
    var items: MutableMap<String, FallbackDefectDraft>,
    var order: MutableList<String>,
) : FallbackDefectDraftStore {
    override fun find(code: String): FallbackDefectDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDefectDraft) {
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

    override fun list(): MutableList<FallbackDefectDraft> {
        val out = mutableListOf<FallbackDefectDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDefectDraftStore(): FallbackDefectDraftStore {
    return MemoryFallbackDefectDraftStore(mutableMapOf<String, FallbackDefectDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDefectDraftStore(
    var inner: FallbackDefectDraftStore,
    var cache: TtlCache<FallbackDefectDraft>,
    var hits: Int,
    var misses: Int,
) : FallbackDefectDraftStore {
    override fun find(code: String): FallbackDefectDraft? {
        val hit = (cache.get(code) as FallbackDefectDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDefectDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDefectDraft> = inner.list()
}

fun newCachedFallbackDefectDraftStore(inner: FallbackDefectDraftStore, ttl: Int, clock: Clock): FallbackDefectDraftStore {
    return CachedFallbackDefectDraftStore(
        inner = inner,
        cache = newTtlCache<FallbackDefectDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback defect drafts. Empty fields match all. */
data class FallbackDefectDraftQuery(
    var locale: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDefectDraft): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDefectDraft>): MutableList<FallbackDefectDraft> {
        val matched = filter(items) { candidate: FallbackDefectDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDefectDrafts(store: FallbackDefectDraftStore, query: FallbackDefectDraftQuery): MutableList<FallbackDefectDraft> {
    return query.apply(store.list())
}
