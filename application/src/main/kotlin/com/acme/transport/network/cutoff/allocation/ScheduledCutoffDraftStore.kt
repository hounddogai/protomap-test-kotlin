package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled cutoff drafts by code. */
interface ScheduledCutoffDraftStore {
    fun find(code: String): ScheduledCutoffDraft?
    fun save(item: ScheduledCutoffDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCutoffDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCutoffDraftStore(
    var items: MutableMap<String, ScheduledCutoffDraft>,
    var order: MutableList<String>,
) : ScheduledCutoffDraftStore {
    override fun find(code: String): ScheduledCutoffDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCutoffDraft) {
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

    override fun list(): MutableList<ScheduledCutoffDraft> {
        val out = mutableListOf<ScheduledCutoffDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCutoffDraftStore(): ScheduledCutoffDraftStore {
    return MemoryScheduledCutoffDraftStore(
        mutableMapOf<String, ScheduledCutoffDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCutoffDraftStore(
    var inner: ScheduledCutoffDraftStore,
    var cache: TtlCache<ScheduledCutoffDraft>,
    var hits: Int,
    var misses: Int,
) : ScheduledCutoffDraftStore {
    override fun find(code: String): ScheduledCutoffDraft? {
        val hit = (cache.get(code) as ScheduledCutoffDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCutoffDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCutoffDraft> = inner.list()
}

fun newCachedScheduledCutoffDraftStore(inner: ScheduledCutoffDraftStore, ttl: Int, clock: Clock): ScheduledCutoffDraftStore {
    return CachedScheduledCutoffDraftStore(
        inner = inner,
        cache = newTtlCache<ScheduledCutoffDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled cutoff drafts. Empty fields match all. */
data class ScheduledCutoffDraftQuery(
    var depot: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCutoffDraft): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledCutoffDraft>): MutableList<ScheduledCutoffDraft> {
        val matched = filter(items) { candidate: ScheduledCutoffDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCutoffDrafts(store: ScheduledCutoffDraftStore, query: ScheduledCutoffDraftQuery): MutableList<ScheduledCutoffDraft> {
    return query.apply(store.list())
}
