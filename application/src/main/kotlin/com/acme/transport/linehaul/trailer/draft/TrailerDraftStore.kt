package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer drafts by code. */
interface TrailerDraftStore {
    fun find(code: String): TrailerDraft?
    fun save(item: TrailerDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerDraftStore(
    var items: MutableMap<String, TrailerDraft>,
    var order: MutableList<String>,
) : TrailerDraftStore {
    override fun find(code: String): TrailerDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerDraft) {
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

    override fun list(): MutableList<TrailerDraft> {
        val out = mutableListOf<TrailerDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerDraftStore(): TrailerDraftStore {
    return MemoryTrailerDraftStore(mutableMapOf<String, TrailerDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerDraftStore(
    var inner: TrailerDraftStore,
    var cache: TtlCache<TrailerDraft>,
    var hits: Int,
    var misses: Int,
) : TrailerDraftStore {
    override fun find(code: String): TrailerDraft? {
        val hit = (cache.get(code) as TrailerDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerDraft> = inner.list()
}

fun newCachedTrailerDraftStore(inner: TrailerDraftStore, ttl: Int, clock: Clock): TrailerDraftStore {
    return CachedTrailerDraftStore(
        inner = inner,
        cache = newTtlCache<TrailerDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer drafts. Empty fields match all. */
data class TrailerDraftQuery(
    var segment: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerDraft): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<TrailerDraft>): MutableList<TrailerDraft> {
        val matched = filter(items) { candidate: TrailerDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerDrafts(store: TrailerDraftStore, query: TrailerDraftQuery): MutableList<TrailerDraft> {
    return query.apply(store.list())
}
