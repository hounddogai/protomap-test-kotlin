package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt drafts by code. */
interface AttemptDraftStore {
    fun find(code: String): AttemptDraft?
    fun save(item: AttemptDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptDraftStore(
    var items: MutableMap<String, AttemptDraft>,
    var order: MutableList<String>,
) : AttemptDraftStore {
    override fun find(code: String): AttemptDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptDraft) {
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

    override fun list(): MutableList<AttemptDraft> {
        val out = mutableListOf<AttemptDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptDraftStore(): AttemptDraftStore {
    return MemoryAttemptDraftStore(mutableMapOf<String, AttemptDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptDraftStore(
    var inner: AttemptDraftStore,
    var cache: TtlCache<AttemptDraft>,
    var hits: Int,
    var misses: Int,
) : AttemptDraftStore {
    override fun find(code: String): AttemptDraft? {
        val hit = (cache.get(code) as AttemptDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptDraft> = inner.list()
}

fun newCachedAttemptDraftStore(inner: AttemptDraftStore, ttl: Int, clock: Clock): AttemptDraftStore {
    return CachedAttemptDraftStore(
        inner = inner,
        cache = newTtlCache<AttemptDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt drafts. Empty fields match all. */
data class AttemptDraftQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptDraft): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptDraft>): MutableList<AttemptDraft> {
        val matched = filter(items) { candidate: AttemptDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptDrafts(store: AttemptDraftStore, query: AttemptDraftQuery): MutableList<AttemptDraft> {
    return query.apply(store.list())
}
