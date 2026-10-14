package com.acme.transport.customs.declaration.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal declaration schedules by code. */
interface SeasonalDeclarationScheduleStore {
    fun find(code: String): SeasonalDeclarationSchedule?
    fun save(item: SeasonalDeclarationSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDeclarationSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDeclarationScheduleStore(
    var items: MutableMap<String, SeasonalDeclarationSchedule>,
    var order: MutableList<String>,
) : SeasonalDeclarationScheduleStore {
    override fun find(code: String): SeasonalDeclarationSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDeclarationSchedule) {
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

    override fun list(): MutableList<SeasonalDeclarationSchedule> {
        val out = mutableListOf<SeasonalDeclarationSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDeclarationScheduleStore(): SeasonalDeclarationScheduleStore {
    return MemorySeasonalDeclarationScheduleStore(
        mutableMapOf<String, SeasonalDeclarationSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDeclarationScheduleStore(
    var inner: SeasonalDeclarationScheduleStore,
    var cache: TtlCache<SeasonalDeclarationSchedule>,
    var hits: Int,
    var misses: Int,
) : SeasonalDeclarationScheduleStore {
    override fun find(code: String): SeasonalDeclarationSchedule? {
        val hit = (cache.get(code) as SeasonalDeclarationSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDeclarationSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDeclarationSchedule> = inner.list()
}

fun newCachedSeasonalDeclarationScheduleStore(inner: SeasonalDeclarationScheduleStore, ttl: Int, clock: Clock): SeasonalDeclarationScheduleStore {
    return CachedSeasonalDeclarationScheduleStore(
        inner = inner,
        cache = newTtlCache<SeasonalDeclarationSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal declaration schedules. Empty fields match all. */
data class SeasonalDeclarationScheduleQuery(
    var category: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDeclarationSchedule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<SeasonalDeclarationSchedule>): MutableList<SeasonalDeclarationSchedule> {
        val matched = filter(items) { candidate: SeasonalDeclarationSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDeclarationSchedules(store: SeasonalDeclarationScheduleStore, query: SeasonalDeclarationScheduleQuery): MutableList<SeasonalDeclarationSchedule> {
    return query.apply(store.list())
}
