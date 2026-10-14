package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break drafts by code. */
interface RestBreakDraftStore {
    fun find(code: String): RestBreakDraft?
    fun save(item: RestBreakDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakDraftStore(
    var items: MutableMap<String, RestBreakDraft>,
    var order: MutableList<String>,
) : RestBreakDraftStore {
    override fun find(code: String): RestBreakDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakDraft) {
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

    override fun list(): MutableList<RestBreakDraft> {
        val out = mutableListOf<RestBreakDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakDraftStore(): RestBreakDraftStore {
    return MemoryRestBreakDraftStore(mutableMapOf<String, RestBreakDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakDraftStore(
    var inner: RestBreakDraftStore,
    var cache: TtlCache<RestBreakDraft>,
    var hits: Int,
    var misses: Int,
) : RestBreakDraftStore {
    override fun find(code: String): RestBreakDraft? {
        val hit = (cache.get(code) as RestBreakDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakDraft> = inner.list()
}

fun newCachedRestBreakDraftStore(inner: RestBreakDraftStore, ttl: Int, clock: Clock): RestBreakDraftStore {
    return CachedRestBreakDraftStore(
        inner = inner,
        cache = newTtlCache<RestBreakDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break drafts. Empty fields match all. */
data class RestBreakDraftQuery(
    var category: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakDraft): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakDraft>): MutableList<RestBreakDraft> {
        val matched = filter(items) { candidate: RestBreakDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakDrafts(store: RestBreakDraftStore, query: RestBreakDraftQuery): MutableList<RestBreakDraft> {
    return query.apply(store.list())
}
