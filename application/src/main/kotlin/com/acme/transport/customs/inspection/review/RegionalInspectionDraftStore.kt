package com.acme.transport.customs.inspection.review

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional inspection drafts by code. */
interface RegionalInspectionDraftStore {
    fun find(code: String): RegionalInspectionDraft?
    fun save(item: RegionalInspectionDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalInspectionDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalInspectionDraftStore(
    var items: MutableMap<String, RegionalInspectionDraft>,
    var order: MutableList<String>,
) : RegionalInspectionDraftStore {
    override fun find(code: String): RegionalInspectionDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalInspectionDraft) {
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

    override fun list(): MutableList<RegionalInspectionDraft> {
        val out = mutableListOf<RegionalInspectionDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalInspectionDraftStore(): RegionalInspectionDraftStore {
    return MemoryRegionalInspectionDraftStore(
        mutableMapOf<String, RegionalInspectionDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalInspectionDraftStore(
    var inner: RegionalInspectionDraftStore,
    var cache: TtlCache<RegionalInspectionDraft>,
    var hits: Int,
    var misses: Int,
) : RegionalInspectionDraftStore {
    override fun find(code: String): RegionalInspectionDraft? {
        val hit = (cache.get(code) as RegionalInspectionDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalInspectionDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalInspectionDraft> = inner.list()
}

fun newCachedRegionalInspectionDraftStore(inner: RegionalInspectionDraftStore, ttl: Int, clock: Clock): RegionalInspectionDraftStore {
    return CachedRegionalInspectionDraftStore(
        inner = inner,
        cache = newTtlCache<RegionalInspectionDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional inspection drafts. Empty fields match all. */
data class RegionalInspectionDraftQuery(
    var channel: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalInspectionDraft): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalInspectionDraft>): MutableList<RegionalInspectionDraft> {
        val matched = filter(items) { candidate: RegionalInspectionDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalInspectionDrafts(store: RegionalInspectionDraftStore, query: RegionalInspectionDraftQuery): MutableList<RegionalInspectionDraft> {
    return query.apply(store.list())
}
