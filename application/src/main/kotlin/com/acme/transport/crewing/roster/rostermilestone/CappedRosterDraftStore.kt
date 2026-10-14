package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped roster drafts by code. */
interface CappedRosterDraftStore {
    fun find(code: String): CappedRosterDraft?
    fun save(item: CappedRosterDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRosterDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRosterDraftStore(
    var items: MutableMap<String, CappedRosterDraft>,
    var order: MutableList<String>,
) : CappedRosterDraftStore {
    override fun find(code: String): CappedRosterDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRosterDraft) {
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

    override fun list(): MutableList<CappedRosterDraft> {
        val out = mutableListOf<CappedRosterDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRosterDraftStore(): CappedRosterDraftStore {
    return MemoryCappedRosterDraftStore(mutableMapOf<String, CappedRosterDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRosterDraftStore(
    var inner: CappedRosterDraftStore,
    var cache: TtlCache<CappedRosterDraft>,
    var hits: Int,
    var misses: Int,
) : CappedRosterDraftStore {
    override fun find(code: String): CappedRosterDraft? {
        val hit = (cache.get(code) as CappedRosterDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRosterDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRosterDraft> = inner.list()
}

fun newCachedCappedRosterDraftStore(inner: CappedRosterDraftStore, ttl: Int, clock: Clock): CappedRosterDraftStore {
    return CachedCappedRosterDraftStore(
        inner = inner,
        cache = newTtlCache<CappedRosterDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped roster drafts. Empty fields match all. */
data class CappedRosterDraftQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRosterDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedRosterDraft>): MutableList<CappedRosterDraft> {
        val matched = filter(items) { candidate: CappedRosterDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRosterDrafts(store: CappedRosterDraftStore, query: CappedRosterDraftQuery): MutableList<CappedRosterDraft> {
    return query.apply(store.list())
}
