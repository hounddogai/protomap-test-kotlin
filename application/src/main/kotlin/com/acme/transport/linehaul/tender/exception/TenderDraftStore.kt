package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender drafts by code. */
interface TenderDraftStore {
    fun find(code: String): TenderDraft?
    fun save(item: TenderDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderDraftStore(
    var items: MutableMap<String, TenderDraft>,
    var order: MutableList<String>,
) : TenderDraftStore {
    override fun find(code: String): TenderDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderDraft) {
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

    override fun list(): MutableList<TenderDraft> {
        val out = mutableListOf<TenderDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderDraftStore(): TenderDraftStore {
    return MemoryTenderDraftStore(mutableMapOf<String, TenderDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderDraftStore(
    var inner: TenderDraftStore,
    var cache: TtlCache<TenderDraft>,
    var hits: Int,
    var misses: Int,
) : TenderDraftStore {
    override fun find(code: String): TenderDraft? {
        val hit = (cache.get(code) as TenderDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderDraft> = inner.list()
}

fun newCachedTenderDraftStore(inner: TenderDraftStore, ttl: Int, clock: Clock): TenderDraftStore {
    return CachedTenderDraftStore(
        inner = inner,
        cache = newTtlCache<TenderDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender drafts. Empty fields match all. */
data class TenderDraftQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderDraft>): MutableList<TenderDraft> {
        val matched = filter(items) { candidate: TenderDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderDrafts(store: TenderDraftStore, query: TenderDraftQuery): MutableList<TenderDraft> {
    return query.apply(store.list())
}
