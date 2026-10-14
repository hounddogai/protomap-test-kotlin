package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard drafts by code. */
interface ScorecardDraftStore {
    fun find(code: String): ScorecardDraft?
    fun save(item: ScorecardDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardDraftStore(
    var items: MutableMap<String, ScorecardDraft>,
    var order: MutableList<String>,
) : ScorecardDraftStore {
    override fun find(code: String): ScorecardDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardDraft) {
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

    override fun list(): MutableList<ScorecardDraft> {
        val out = mutableListOf<ScorecardDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardDraftStore(): ScorecardDraftStore {
    return MemoryScorecardDraftStore(mutableMapOf<String, ScorecardDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardDraftStore(
    var inner: ScorecardDraftStore,
    var cache: TtlCache<ScorecardDraft>,
    var hits: Int,
    var misses: Int,
) : ScorecardDraftStore {
    override fun find(code: String): ScorecardDraft? {
        val hit = (cache.get(code) as ScorecardDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardDraft> = inner.list()
}

fun newCachedScorecardDraftStore(inner: ScorecardDraftStore, ttl: Int, clock: Clock): ScorecardDraftStore {
    return CachedScorecardDraftStore(
        inner = inner,
        cache = newTtlCache<ScorecardDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard drafts. Empty fields match all. */
data class ScorecardDraftQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardDraft): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardDraft>): MutableList<ScorecardDraft> {
        val matched = filter(items) { candidate: ScorecardDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardDrafts(store: ScorecardDraftStore, query: ScorecardDraftQuery): MutableList<ScorecardDraft> {
    return query.apply(store.list())
}
