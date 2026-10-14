package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict rest break drafts by code. */
interface StrictRestBreakDraftStore {
    fun find(code: String): StrictRestBreakDraft?
    fun save(item: StrictRestBreakDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRestBreakDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRestBreakDraftStore(
    var items: MutableMap<String, StrictRestBreakDraft>,
    var order: MutableList<String>,
) : StrictRestBreakDraftStore {
    override fun find(code: String): StrictRestBreakDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRestBreakDraft) {
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

    override fun list(): MutableList<StrictRestBreakDraft> {
        val out = mutableListOf<StrictRestBreakDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRestBreakDraftStore(): StrictRestBreakDraftStore {
    return MemoryStrictRestBreakDraftStore(
        mutableMapOf<String, StrictRestBreakDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRestBreakDraftStore(
    var inner: StrictRestBreakDraftStore,
    var cache: TtlCache<StrictRestBreakDraft>,
    var hits: Int,
    var misses: Int,
) : StrictRestBreakDraftStore {
    override fun find(code: String): StrictRestBreakDraft? {
        val hit = (cache.get(code) as StrictRestBreakDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRestBreakDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRestBreakDraft> = inner.list()
}

fun newCachedStrictRestBreakDraftStore(inner: StrictRestBreakDraftStore, ttl: Int, clock: Clock): StrictRestBreakDraftStore {
    return CachedStrictRestBreakDraftStore(
        inner = inner,
        cache = newTtlCache<StrictRestBreakDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict rest break drafts. Empty fields match all. */
data class StrictRestBreakDraftQuery(
    var segment: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRestBreakDraft): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<StrictRestBreakDraft>): MutableList<StrictRestBreakDraft> {
        val matched = filter(items) { candidate: StrictRestBreakDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRestBreakDrafts(store: StrictRestBreakDraftStore, query: StrictRestBreakDraftQuery): MutableList<StrictRestBreakDraft> {
    return query.apply(store.list())
}
