package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary trailer yard drafts by code. */
interface PrimaryTrailerYardDraftStore {
    fun find(code: String): PrimaryTrailerYardDraft?
    fun save(item: PrimaryTrailerYardDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTrailerYardDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTrailerYardDraftStore(
    var items: MutableMap<String, PrimaryTrailerYardDraft>,
    var order: MutableList<String>,
) : PrimaryTrailerYardDraftStore {
    override fun find(code: String): PrimaryTrailerYardDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTrailerYardDraft) {
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

    override fun list(): MutableList<PrimaryTrailerYardDraft> {
        val out = mutableListOf<PrimaryTrailerYardDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTrailerYardDraftStore(): PrimaryTrailerYardDraftStore {
    return MemoryPrimaryTrailerYardDraftStore(
        mutableMapOf<String, PrimaryTrailerYardDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTrailerYardDraftStore(
    var inner: PrimaryTrailerYardDraftStore,
    var cache: TtlCache<PrimaryTrailerYardDraft>,
    var hits: Int,
    var misses: Int,
) : PrimaryTrailerYardDraftStore {
    override fun find(code: String): PrimaryTrailerYardDraft? {
        val hit = (cache.get(code) as PrimaryTrailerYardDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTrailerYardDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTrailerYardDraft> = inner.list()
}

fun newCachedPrimaryTrailerYardDraftStore(inner: PrimaryTrailerYardDraftStore, ttl: Int, clock: Clock): PrimaryTrailerYardDraftStore {
    return CachedPrimaryTrailerYardDraftStore(
        inner = inner,
        cache = newTtlCache<PrimaryTrailerYardDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary trailer yard drafts. Empty fields match all. */
data class PrimaryTrailerYardDraftQuery(
    var unit: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTrailerYardDraft): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTrailerYardDraft>): MutableList<PrimaryTrailerYardDraft> {
        val matched = filter(items) { candidate: PrimaryTrailerYardDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTrailerYardDrafts(store: PrimaryTrailerYardDraftStore, query: PrimaryTrailerYardDraftQuery): MutableList<PrimaryTrailerYardDraft> {
    return query.apply(store.list())
}
