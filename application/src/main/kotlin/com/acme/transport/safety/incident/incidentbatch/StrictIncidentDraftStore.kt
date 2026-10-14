package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict incident drafts by code. */
interface StrictIncidentDraftStore {
    fun find(code: String): StrictIncidentDraft?
    fun save(item: StrictIncidentDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictIncidentDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictIncidentDraftStore(
    var items: MutableMap<String, StrictIncidentDraft>,
    var order: MutableList<String>,
) : StrictIncidentDraftStore {
    override fun find(code: String): StrictIncidentDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictIncidentDraft) {
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

    override fun list(): MutableList<StrictIncidentDraft> {
        val out = mutableListOf<StrictIncidentDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictIncidentDraftStore(): StrictIncidentDraftStore {
    return MemoryStrictIncidentDraftStore(mutableMapOf<String, StrictIncidentDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictIncidentDraftStore(
    var inner: StrictIncidentDraftStore,
    var cache: TtlCache<StrictIncidentDraft>,
    var hits: Int,
    var misses: Int,
) : StrictIncidentDraftStore {
    override fun find(code: String): StrictIncidentDraft? {
        val hit = (cache.get(code) as StrictIncidentDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictIncidentDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictIncidentDraft> = inner.list()
}

fun newCachedStrictIncidentDraftStore(inner: StrictIncidentDraftStore, ttl: Int, clock: Clock): StrictIncidentDraftStore {
    return CachedStrictIncidentDraftStore(
        inner = inner,
        cache = newTtlCache<StrictIncidentDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict incident drafts. Empty fields match all. */
data class StrictIncidentDraftQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictIncidentDraft): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<StrictIncidentDraft>): MutableList<StrictIncidentDraft> {
        val matched = filter(items) { candidate: StrictIncidentDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictIncidentDrafts(store: StrictIncidentDraftStore, query: StrictIncidentDraftQuery): MutableList<StrictIncidentDraft> {
    return query.apply(store.list())
}
