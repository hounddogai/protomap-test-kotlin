package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching windows by code. */
interface CoachingWindowStore {
    fun find(code: String): CoachingWindow?
    fun save(item: CoachingWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingWindowStore(
    var items: MutableMap<String, CoachingWindow>,
    var order: MutableList<String>,
) : CoachingWindowStore {
    override fun find(code: String): CoachingWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingWindow) {
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

    override fun list(): MutableList<CoachingWindow> {
        val out = mutableListOf<CoachingWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingWindowStore(): CoachingWindowStore {
    return MemoryCoachingWindowStore(mutableMapOf<String, CoachingWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingWindowStore(
    var inner: CoachingWindowStore,
    var cache: TtlCache<CoachingWindow>,
    var hits: Int,
    var misses: Int,
) : CoachingWindowStore {
    override fun find(code: String): CoachingWindow? {
        val hit = (cache.get(code) as CoachingWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingWindow> = inner.list()
}

fun newCachedCoachingWindowStore(inner: CoachingWindowStore, ttl: Int, clock: Clock): CoachingWindowStore {
    return CachedCoachingWindowStore(
        inner = inner,
        cache = newTtlCache<CoachingWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching windows. Empty fields match all. */
data class CoachingWindowQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingWindow>): MutableList<CoachingWindow> {
        val matched = filter(items) { candidate: CoachingWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingWindows(store: CoachingWindowStore, query: CoachingWindowQuery): MutableList<CoachingWindow> {
    return query.apply(store.list())
}
