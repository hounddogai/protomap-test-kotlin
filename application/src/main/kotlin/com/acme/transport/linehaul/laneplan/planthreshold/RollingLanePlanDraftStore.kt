package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling lane plan drafts by code. */
interface RollingLanePlanDraftStore {
    fun find(code: String): RollingLanePlanDraft?
    fun save(item: RollingLanePlanDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingLanePlanDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingLanePlanDraftStore(
    var items: MutableMap<String, RollingLanePlanDraft>,
    var order: MutableList<String>,
) : RollingLanePlanDraftStore {
    override fun find(code: String): RollingLanePlanDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingLanePlanDraft) {
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

    override fun list(): MutableList<RollingLanePlanDraft> {
        val out = mutableListOf<RollingLanePlanDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingLanePlanDraftStore(): RollingLanePlanDraftStore {
    return MemoryRollingLanePlanDraftStore(
        mutableMapOf<String, RollingLanePlanDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingLanePlanDraftStore(
    var inner: RollingLanePlanDraftStore,
    var cache: TtlCache<RollingLanePlanDraft>,
    var hits: Int,
    var misses: Int,
) : RollingLanePlanDraftStore {
    override fun find(code: String): RollingLanePlanDraft? {
        val hit = (cache.get(code) as RollingLanePlanDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingLanePlanDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingLanePlanDraft> = inner.list()
}

fun newCachedRollingLanePlanDraftStore(inner: RollingLanePlanDraftStore, ttl: Int, clock: Clock): RollingLanePlanDraftStore {
    return CachedRollingLanePlanDraftStore(
        inner = inner,
        cache = newTtlCache<RollingLanePlanDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling lane plan drafts. Empty fields match all. */
data class RollingLanePlanDraftQuery(
    var depot: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingLanePlanDraft): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingLanePlanDraft>): MutableList<RollingLanePlanDraft> {
        val matched = filter(items) { candidate: RollingLanePlanDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingLanePlanDrafts(store: RollingLanePlanDraftStore, query: RollingLanePlanDraftQuery): MutableList<RollingLanePlanDraft> {
    return query.apply(store.list())
}
