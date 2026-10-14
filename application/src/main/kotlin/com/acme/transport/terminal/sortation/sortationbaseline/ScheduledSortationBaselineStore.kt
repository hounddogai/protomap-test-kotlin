package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled sortation baselines by code. */
interface ScheduledSortationBaselineStore {
    fun find(code: String): ScheduledSortationBaseline?
    fun save(item: ScheduledSortationBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledSortationBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledSortationBaselineStore(
    var items: MutableMap<String, ScheduledSortationBaseline>,
    var order: MutableList<String>,
) : ScheduledSortationBaselineStore {
    override fun find(code: String): ScheduledSortationBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledSortationBaseline) {
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

    override fun list(): MutableList<ScheduledSortationBaseline> {
        val out = mutableListOf<ScheduledSortationBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledSortationBaselineStore(): ScheduledSortationBaselineStore {
    return MemoryScheduledSortationBaselineStore(
        mutableMapOf<String, ScheduledSortationBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledSortationBaselineStore(
    var inner: ScheduledSortationBaselineStore,
    var cache: TtlCache<ScheduledSortationBaseline>,
    var hits: Int,
    var misses: Int,
) : ScheduledSortationBaselineStore {
    override fun find(code: String): ScheduledSortationBaseline? {
        val hit = (cache.get(code) as ScheduledSortationBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledSortationBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledSortationBaseline> = inner.list()
}

fun newCachedScheduledSortationBaselineStore(inner: ScheduledSortationBaselineStore, ttl: Int, clock: Clock): ScheduledSortationBaselineStore {
    return CachedScheduledSortationBaselineStore(
        inner = inner,
        cache = newTtlCache<ScheduledSortationBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled sortation baselines. Empty fields match all. */
data class ScheduledSortationBaselineQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledSortationBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledSortationBaseline>): MutableList<ScheduledSortationBaseline> {
        val matched = filter(items) { candidate: ScheduledSortationBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledSortationBaselines(store: ScheduledSortationBaselineStore, query: ScheduledSortationBaselineQuery): MutableList<ScheduledSortationBaseline> {
    return query.apply(store.list())
}
