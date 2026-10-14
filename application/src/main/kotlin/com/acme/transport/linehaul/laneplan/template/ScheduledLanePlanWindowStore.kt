package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled lane plan windows by code. */
interface ScheduledLanePlanWindowStore {
    fun find(code: String): ScheduledLanePlanWindow?
    fun save(item: ScheduledLanePlanWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLanePlanWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLanePlanWindowStore(
    var items: MutableMap<String, ScheduledLanePlanWindow>,
    var order: MutableList<String>,
) : ScheduledLanePlanWindowStore {
    override fun find(code: String): ScheduledLanePlanWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLanePlanWindow) {
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

    override fun list(): MutableList<ScheduledLanePlanWindow> {
        val out = mutableListOf<ScheduledLanePlanWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLanePlanWindowStore(): ScheduledLanePlanWindowStore {
    return MemoryScheduledLanePlanWindowStore(
        mutableMapOf<String, ScheduledLanePlanWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLanePlanWindowStore(
    var inner: ScheduledLanePlanWindowStore,
    var cache: TtlCache<ScheduledLanePlanWindow>,
    var hits: Int,
    var misses: Int,
) : ScheduledLanePlanWindowStore {
    override fun find(code: String): ScheduledLanePlanWindow? {
        val hit = (cache.get(code) as ScheduledLanePlanWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLanePlanWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLanePlanWindow> = inner.list()
}

fun newCachedScheduledLanePlanWindowStore(inner: ScheduledLanePlanWindowStore, ttl: Int, clock: Clock): ScheduledLanePlanWindowStore {
    return CachedScheduledLanePlanWindowStore(
        inner = inner,
        cache = newTtlCache<ScheduledLanePlanWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled lane plan windows. Empty fields match all. */
data class ScheduledLanePlanWindowQuery(
    var depot: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLanePlanWindow): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<ScheduledLanePlanWindow>): MutableList<ScheduledLanePlanWindow> {
        val matched = filter(items) { candidate: ScheduledLanePlanWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLanePlanWindows(store: ScheduledLanePlanWindowStore, query: ScheduledLanePlanWindowQuery): MutableList<ScheduledLanePlanWindow> {
    return query.apply(store.list())
}
