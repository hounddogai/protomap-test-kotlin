package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling duty period drafts by code. */
interface RollingDutyPeriodDraftStore {
    fun find(code: String): RollingDutyPeriodDraft?
    fun save(item: RollingDutyPeriodDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDutyPeriodDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDutyPeriodDraftStore(
    var items: MutableMap<String, RollingDutyPeriodDraft>,
    var order: MutableList<String>,
) : RollingDutyPeriodDraftStore {
    override fun find(code: String): RollingDutyPeriodDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDutyPeriodDraft) {
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

    override fun list(): MutableList<RollingDutyPeriodDraft> {
        val out = mutableListOf<RollingDutyPeriodDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDutyPeriodDraftStore(): RollingDutyPeriodDraftStore {
    return MemoryRollingDutyPeriodDraftStore(
        mutableMapOf<String, RollingDutyPeriodDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDutyPeriodDraftStore(
    var inner: RollingDutyPeriodDraftStore,
    var cache: TtlCache<RollingDutyPeriodDraft>,
    var hits: Int,
    var misses: Int,
) : RollingDutyPeriodDraftStore {
    override fun find(code: String): RollingDutyPeriodDraft? {
        val hit = (cache.get(code) as RollingDutyPeriodDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDutyPeriodDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDutyPeriodDraft> = inner.list()
}

fun newCachedRollingDutyPeriodDraftStore(inner: RollingDutyPeriodDraftStore, ttl: Int, clock: Clock): RollingDutyPeriodDraftStore {
    return CachedRollingDutyPeriodDraftStore(
        inner = inner,
        cache = newTtlCache<RollingDutyPeriodDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling duty period drafts. Empty fields match all. */
data class RollingDutyPeriodDraftQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDutyPeriodDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDutyPeriodDraft>): MutableList<RollingDutyPeriodDraft> {
        val matched = filter(items) { candidate: RollingDutyPeriodDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDutyPeriodDrafts(store: RollingDutyPeriodDraftStore, query: RollingDutyPeriodDraftQuery): MutableList<RollingDutyPeriodDraft> {
    return query.apply(store.list())
}
