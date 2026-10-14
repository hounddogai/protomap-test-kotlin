package com.acme.transport.crewing.qualification.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional qualification drafts by code. */
interface RegionalQualificationDraftStore {
    fun find(code: String): RegionalQualificationDraft?
    fun save(item: RegionalQualificationDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalQualificationDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalQualificationDraftStore(
    var items: MutableMap<String, RegionalQualificationDraft>,
    var order: MutableList<String>,
) : RegionalQualificationDraftStore {
    override fun find(code: String): RegionalQualificationDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalQualificationDraft) {
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

    override fun list(): MutableList<RegionalQualificationDraft> {
        val out = mutableListOf<RegionalQualificationDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalQualificationDraftStore(): RegionalQualificationDraftStore {
    return MemoryRegionalQualificationDraftStore(
        mutableMapOf<String, RegionalQualificationDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalQualificationDraftStore(
    var inner: RegionalQualificationDraftStore,
    var cache: TtlCache<RegionalQualificationDraft>,
    var hits: Int,
    var misses: Int,
) : RegionalQualificationDraftStore {
    override fun find(code: String): RegionalQualificationDraft? {
        val hit = (cache.get(code) as RegionalQualificationDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalQualificationDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalQualificationDraft> = inner.list()
}

fun newCachedRegionalQualificationDraftStore(inner: RegionalQualificationDraftStore, ttl: Int, clock: Clock): RegionalQualificationDraftStore {
    return CachedRegionalQualificationDraftStore(
        inner = inner,
        cache = newTtlCache<RegionalQualificationDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional qualification drafts. Empty fields match all. */
data class RegionalQualificationDraftQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalQualificationDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RegionalQualificationDraft>): MutableList<RegionalQualificationDraft> {
        val matched = filter(items) { candidate: RegionalQualificationDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalQualificationDrafts(store: RegionalQualificationDraftStore, query: RegionalQualificationDraftQuery): MutableList<RegionalQualificationDraft> {
    return query.apply(store.list())
}
