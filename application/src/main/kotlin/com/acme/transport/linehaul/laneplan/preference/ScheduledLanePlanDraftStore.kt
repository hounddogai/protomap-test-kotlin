package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled lane plan drafts by code. */
interface ScheduledLanePlanDraftStore {
    fun find(code: String): ScheduledLanePlanDraft?
    fun save(item: ScheduledLanePlanDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLanePlanDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLanePlanDraftStore(
    var items: MutableMap<String, ScheduledLanePlanDraft>,
    var order: MutableList<String>,
) : ScheduledLanePlanDraftStore {
    override fun find(code: String): ScheduledLanePlanDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLanePlanDraft) {
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

    override fun list(): MutableList<ScheduledLanePlanDraft> {
        val out = mutableListOf<ScheduledLanePlanDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLanePlanDraftStore(): ScheduledLanePlanDraftStore {
    return MemoryScheduledLanePlanDraftStore(
        mutableMapOf<String, ScheduledLanePlanDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLanePlanDraftStore(
    var inner: ScheduledLanePlanDraftStore,
    var cache: TtlCache<ScheduledLanePlanDraft>,
    var hits: Int,
    var misses: Int,
) : ScheduledLanePlanDraftStore {
    override fun find(code: String): ScheduledLanePlanDraft? {
        val hit = (cache.get(code) as ScheduledLanePlanDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLanePlanDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLanePlanDraft> = inner.list()
}

fun newCachedScheduledLanePlanDraftStore(inner: ScheduledLanePlanDraftStore, ttl: Int, clock: Clock): ScheduledLanePlanDraftStore {
    return CachedScheduledLanePlanDraftStore(
        inner = inner,
        cache = newTtlCache<ScheduledLanePlanDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled lane plan drafts. Empty fields match all. */
data class ScheduledLanePlanDraftQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLanePlanDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLanePlanDraft>): MutableList<ScheduledLanePlanDraft> {
        val matched = filter(items) { candidate: ScheduledLanePlanDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLanePlanDrafts(store: ScheduledLanePlanDraftStore, query: ScheduledLanePlanDraftQuery): MutableList<ScheduledLanePlanDraft> {
    return query.apply(store.list())
}
