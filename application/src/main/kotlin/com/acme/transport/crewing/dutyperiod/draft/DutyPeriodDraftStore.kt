package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period drafts by code. */
interface DutyPeriodDraftStore {
    fun find(code: String): DutyPeriodDraft?
    fun save(item: DutyPeriodDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodDraftStore(
    var items: MutableMap<String, DutyPeriodDraft>,
    var order: MutableList<String>,
) : DutyPeriodDraftStore {
    override fun find(code: String): DutyPeriodDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodDraft) {
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

    override fun list(): MutableList<DutyPeriodDraft> {
        val out = mutableListOf<DutyPeriodDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodDraftStore(): DutyPeriodDraftStore {
    return MemoryDutyPeriodDraftStore(mutableMapOf<String, DutyPeriodDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodDraftStore(
    var inner: DutyPeriodDraftStore,
    var cache: TtlCache<DutyPeriodDraft>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodDraftStore {
    override fun find(code: String): DutyPeriodDraft? {
        val hit = (cache.get(code) as DutyPeriodDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodDraft> = inner.list()
}

fun newCachedDutyPeriodDraftStore(inner: DutyPeriodDraftStore, ttl: Int, clock: Clock): DutyPeriodDraftStore {
    return CachedDutyPeriodDraftStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period drafts. Empty fields match all. */
data class DutyPeriodDraftQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodDraft>): MutableList<DutyPeriodDraft> {
        val matched = filter(items) { candidate: DutyPeriodDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodDrafts(store: DutyPeriodDraftStore, query: DutyPeriodDraftQuery): MutableList<DutyPeriodDraft> {
    return query.apply(store.list())
}
