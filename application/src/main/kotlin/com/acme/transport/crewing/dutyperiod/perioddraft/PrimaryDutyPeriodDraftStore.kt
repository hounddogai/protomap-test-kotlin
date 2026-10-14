package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary duty period drafts by code. */
interface PrimaryDutyPeriodDraftStore {
    fun find(code: String): PrimaryDutyPeriodDraft?
    fun save(item: PrimaryDutyPeriodDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDutyPeriodDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDutyPeriodDraftStore(
    var items: MutableMap<String, PrimaryDutyPeriodDraft>,
    var order: MutableList<String>,
) : PrimaryDutyPeriodDraftStore {
    override fun find(code: String): PrimaryDutyPeriodDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDutyPeriodDraft) {
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

    override fun list(): MutableList<PrimaryDutyPeriodDraft> {
        val out = mutableListOf<PrimaryDutyPeriodDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDutyPeriodDraftStore(): PrimaryDutyPeriodDraftStore {
    return MemoryPrimaryDutyPeriodDraftStore(
        mutableMapOf<String, PrimaryDutyPeriodDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDutyPeriodDraftStore(
    var inner: PrimaryDutyPeriodDraftStore,
    var cache: TtlCache<PrimaryDutyPeriodDraft>,
    var hits: Int,
    var misses: Int,
) : PrimaryDutyPeriodDraftStore {
    override fun find(code: String): PrimaryDutyPeriodDraft? {
        val hit = (cache.get(code) as PrimaryDutyPeriodDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDutyPeriodDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDutyPeriodDraft> = inner.list()
}

fun newCachedPrimaryDutyPeriodDraftStore(inner: PrimaryDutyPeriodDraftStore, ttl: Int, clock: Clock): PrimaryDutyPeriodDraftStore {
    return CachedPrimaryDutyPeriodDraftStore(
        inner = inner,
        cache = newTtlCache<PrimaryDutyPeriodDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary duty period drafts. Empty fields match all. */
data class PrimaryDutyPeriodDraftQuery(
    var locale: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDutyPeriodDraft): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryDutyPeriodDraft>): MutableList<PrimaryDutyPeriodDraft> {
        val matched = filter(items) { candidate: PrimaryDutyPeriodDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDutyPeriodDrafts(store: PrimaryDutyPeriodDraftStore, query: PrimaryDutyPeriodDraftQuery): MutableList<PrimaryDutyPeriodDraft> {
    return query.apply(store.list())
}
