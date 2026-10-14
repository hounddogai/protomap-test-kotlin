package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary tender drafts by code. */
interface PrimaryTenderDraftStore {
    fun find(code: String): PrimaryTenderDraft?
    fun save(item: PrimaryTenderDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTenderDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTenderDraftStore(
    var items: MutableMap<String, PrimaryTenderDraft>,
    var order: MutableList<String>,
) : PrimaryTenderDraftStore {
    override fun find(code: String): PrimaryTenderDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTenderDraft) {
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

    override fun list(): MutableList<PrimaryTenderDraft> {
        val out = mutableListOf<PrimaryTenderDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTenderDraftStore(): PrimaryTenderDraftStore {
    return MemoryPrimaryTenderDraftStore(mutableMapOf<String, PrimaryTenderDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTenderDraftStore(
    var inner: PrimaryTenderDraftStore,
    var cache: TtlCache<PrimaryTenderDraft>,
    var hits: Int,
    var misses: Int,
) : PrimaryTenderDraftStore {
    override fun find(code: String): PrimaryTenderDraft? {
        val hit = (cache.get(code) as PrimaryTenderDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTenderDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTenderDraft> = inner.list()
}

fun newCachedPrimaryTenderDraftStore(inner: PrimaryTenderDraftStore, ttl: Int, clock: Clock): PrimaryTenderDraftStore {
    return CachedPrimaryTenderDraftStore(
        inner = inner,
        cache = newTtlCache<PrimaryTenderDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tender drafts. Empty fields match all. */
data class PrimaryTenderDraftQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTenderDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<PrimaryTenderDraft>): MutableList<PrimaryTenderDraft> {
        val matched = filter(items) { candidate: PrimaryTenderDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTenderDrafts(store: PrimaryTenderDraftStore, query: PrimaryTenderDraftQuery): MutableList<PrimaryTenderDraft> {
    return query.apply(store.list())
}
