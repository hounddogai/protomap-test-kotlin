package com.acme.transport.fleetops.defect.review

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect drafts by code. */
interface DefectDraftStore {
    fun find(code: String): DefectDraft?
    fun save(item: DefectDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectDraftStore(
    var items: MutableMap<String, DefectDraft>,
    var order: MutableList<String>,
) : DefectDraftStore {
    override fun find(code: String): DefectDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectDraft) {
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

    override fun list(): MutableList<DefectDraft> {
        val out = mutableListOf<DefectDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectDraftStore(): DefectDraftStore {
    return MemoryDefectDraftStore(mutableMapOf<String, DefectDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectDraftStore(
    var inner: DefectDraftStore,
    var cache: TtlCache<DefectDraft>,
    var hits: Int,
    var misses: Int,
) : DefectDraftStore {
    override fun find(code: String): DefectDraft? {
        val hit = (cache.get(code) as DefectDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectDraft> = inner.list()
}

fun newCachedDefectDraftStore(inner: DefectDraftStore, ttl: Int, clock: Clock): DefectDraftStore {
    return CachedDefectDraftStore(
        inner = inner,
        cache = newTtlCache<DefectDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect drafts. Empty fields match all. */
data class DefectDraftQuery(
    var segment: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectDraft): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectDraft>): MutableList<DefectDraft> {
        val matched = filter(items) { candidate: DefectDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectDrafts(store: DefectDraftStore, query: DefectDraftQuery): MutableList<DefectDraft> {
    return query.apply(store.list())
}
