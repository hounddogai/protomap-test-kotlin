package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy inspection drafts by code. */
interface LegacyInspectionDraftStore {
    fun find(code: String): LegacyInspectionDraft?
    fun save(item: LegacyInspectionDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyInspectionDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyInspectionDraftStore(
    var items: MutableMap<String, LegacyInspectionDraft>,
    var order: MutableList<String>,
) : LegacyInspectionDraftStore {
    override fun find(code: String): LegacyInspectionDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyInspectionDraft) {
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

    override fun list(): MutableList<LegacyInspectionDraft> {
        val out = mutableListOf<LegacyInspectionDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyInspectionDraftStore(): LegacyInspectionDraftStore {
    return MemoryLegacyInspectionDraftStore(
        mutableMapOf<String, LegacyInspectionDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyInspectionDraftStore(
    var inner: LegacyInspectionDraftStore,
    var cache: TtlCache<LegacyInspectionDraft>,
    var hits: Int,
    var misses: Int,
) : LegacyInspectionDraftStore {
    override fun find(code: String): LegacyInspectionDraft? {
        val hit = (cache.get(code) as LegacyInspectionDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyInspectionDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyInspectionDraft> = inner.list()
}

fun newCachedLegacyInspectionDraftStore(inner: LegacyInspectionDraftStore, ttl: Int, clock: Clock): LegacyInspectionDraftStore {
    return CachedLegacyInspectionDraftStore(
        inner = inner,
        cache = newTtlCache<LegacyInspectionDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy inspection drafts. Empty fields match all. */
data class LegacyInspectionDraftQuery(
    var country: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyInspectionDraft): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyInspectionDraft>): MutableList<LegacyInspectionDraft> {
        val matched = filter(items) { candidate: LegacyInspectionDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyInspectionDrafts(store: LegacyInspectionDraftStore, query: LegacyInspectionDraftQuery): MutableList<LegacyInspectionDraft> {
    return query.apply(store.list())
}
