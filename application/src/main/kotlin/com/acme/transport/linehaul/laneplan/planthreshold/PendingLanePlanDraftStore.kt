package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending lane plan drafts by code. */
interface PendingLanePlanDraftStore {
    fun find(code: String): PendingLanePlanDraft?
    fun save(item: PendingLanePlanDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingLanePlanDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingLanePlanDraftStore(
    var items: MutableMap<String, PendingLanePlanDraft>,
    var order: MutableList<String>,
) : PendingLanePlanDraftStore {
    override fun find(code: String): PendingLanePlanDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingLanePlanDraft) {
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

    override fun list(): MutableList<PendingLanePlanDraft> {
        val out = mutableListOf<PendingLanePlanDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingLanePlanDraftStore(): PendingLanePlanDraftStore {
    return MemoryPendingLanePlanDraftStore(
        mutableMapOf<String, PendingLanePlanDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingLanePlanDraftStore(
    var inner: PendingLanePlanDraftStore,
    var cache: TtlCache<PendingLanePlanDraft>,
    var hits: Int,
    var misses: Int,
) : PendingLanePlanDraftStore {
    override fun find(code: String): PendingLanePlanDraft? {
        val hit = (cache.get(code) as PendingLanePlanDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingLanePlanDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingLanePlanDraft> = inner.list()
}

fun newCachedPendingLanePlanDraftStore(inner: PendingLanePlanDraftStore, ttl: Int, clock: Clock): PendingLanePlanDraftStore {
    return CachedPendingLanePlanDraftStore(
        inner = inner,
        cache = newTtlCache<PendingLanePlanDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending lane plan drafts. Empty fields match all. */
data class PendingLanePlanDraftQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingLanePlanDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingLanePlanDraft>): MutableList<PendingLanePlanDraft> {
        val matched = filter(items) { candidate: PendingLanePlanDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingLanePlanDrafts(store: PendingLanePlanDraftStore, query: PendingLanePlanDraftQuery): MutableList<PendingLanePlanDraft> {
    return query.apply(store.list())
}
