package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global attempt drafts by code. */
interface GlobalAttemptDraftStore {
    fun find(code: String): GlobalAttemptDraft?
    fun save(item: GlobalAttemptDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAttemptDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAttemptDraftStore(
    var items: MutableMap<String, GlobalAttemptDraft>,
    var order: MutableList<String>,
) : GlobalAttemptDraftStore {
    override fun find(code: String): GlobalAttemptDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAttemptDraft) {
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

    override fun list(): MutableList<GlobalAttemptDraft> {
        val out = mutableListOf<GlobalAttemptDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAttemptDraftStore(): GlobalAttemptDraftStore {
    return MemoryGlobalAttemptDraftStore(mutableMapOf<String, GlobalAttemptDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAttemptDraftStore(
    var inner: GlobalAttemptDraftStore,
    var cache: TtlCache<GlobalAttemptDraft>,
    var hits: Int,
    var misses: Int,
) : GlobalAttemptDraftStore {
    override fun find(code: String): GlobalAttemptDraft? {
        val hit = (cache.get(code) as GlobalAttemptDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAttemptDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAttemptDraft> = inner.list()
}

fun newCachedGlobalAttemptDraftStore(inner: GlobalAttemptDraftStore, ttl: Int, clock: Clock): GlobalAttemptDraftStore {
    return CachedGlobalAttemptDraftStore(
        inner = inner,
        cache = newTtlCache<GlobalAttemptDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global attempt drafts. Empty fields match all. */
data class GlobalAttemptDraftQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAttemptDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<GlobalAttemptDraft>): MutableList<GlobalAttemptDraft> {
        val matched = filter(items) { candidate: GlobalAttemptDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAttemptDrafts(store: GlobalAttemptDraftStore, query: GlobalAttemptDraftQuery): MutableList<GlobalAttemptDraft> {
    return query.apply(store.list())
}
