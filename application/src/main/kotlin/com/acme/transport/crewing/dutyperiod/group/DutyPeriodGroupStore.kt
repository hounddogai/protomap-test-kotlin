package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period groups by code. */
interface DutyPeriodGroupStore {
    fun find(code: String): DutyPeriodGroup?
    fun save(item: DutyPeriodGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodGroupStore(
    var items: MutableMap<String, DutyPeriodGroup>,
    var order: MutableList<String>,
) : DutyPeriodGroupStore {
    override fun find(code: String): DutyPeriodGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodGroup) {
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

    override fun list(): MutableList<DutyPeriodGroup> {
        val out = mutableListOf<DutyPeriodGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodGroupStore(): DutyPeriodGroupStore {
    return MemoryDutyPeriodGroupStore(mutableMapOf<String, DutyPeriodGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodGroupStore(
    var inner: DutyPeriodGroupStore,
    var cache: TtlCache<DutyPeriodGroup>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodGroupStore {
    override fun find(code: String): DutyPeriodGroup? {
        val hit = (cache.get(code) as DutyPeriodGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodGroup> = inner.list()
}

fun newCachedDutyPeriodGroupStore(inner: DutyPeriodGroupStore, ttl: Int, clock: Clock): DutyPeriodGroupStore {
    return CachedDutyPeriodGroupStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period groups. Empty fields match all. */
data class DutyPeriodGroupQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodGroup>): MutableList<DutyPeriodGroup> {
        val matched = filter(items) { candidate: DutyPeriodGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodGroups(store: DutyPeriodGroupStore, query: DutyPeriodGroupQuery): MutableList<DutyPeriodGroup> {
    return query.apply(store.list())
}
