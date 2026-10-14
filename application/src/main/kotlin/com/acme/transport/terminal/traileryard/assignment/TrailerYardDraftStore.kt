package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard drafts by code. */
interface TrailerYardDraftStore {
    fun find(code: String): TrailerYardDraft?
    fun save(item: TrailerYardDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardDraftStore(
    var items: MutableMap<String, TrailerYardDraft>,
    var order: MutableList<String>,
) : TrailerYardDraftStore {
    override fun find(code: String): TrailerYardDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardDraft) {
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

    override fun list(): MutableList<TrailerYardDraft> {
        val out = mutableListOf<TrailerYardDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardDraftStore(): TrailerYardDraftStore {
    return MemoryTrailerYardDraftStore(mutableMapOf<String, TrailerYardDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardDraftStore(
    var inner: TrailerYardDraftStore,
    var cache: TtlCache<TrailerYardDraft>,
    var hits: Int,
    var misses: Int,
) : TrailerYardDraftStore {
    override fun find(code: String): TrailerYardDraft? {
        val hit = (cache.get(code) as TrailerYardDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardDraft> = inner.list()
}

fun newCachedTrailerYardDraftStore(inner: TrailerYardDraftStore, ttl: Int, clock: Clock): TrailerYardDraftStore {
    return CachedTrailerYardDraftStore(
        inner = inner,
        cache = newTtlCache<TrailerYardDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard drafts. Empty fields match all. */
data class TrailerYardDraftQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardDraft>): MutableList<TrailerYardDraft> {
        val matched = filter(items) { candidate: TrailerYardDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardDrafts(store: TrailerYardDraftStore, query: TrailerYardDraftQuery): MutableList<TrailerYardDraft> {
    return query.apply(store.list())
}
