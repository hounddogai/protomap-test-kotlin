package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy coaching windows by code. */
interface LegacyCoachingWindowStore {
    fun find(code: String): LegacyCoachingWindow?
    fun save(item: LegacyCoachingWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCoachingWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCoachingWindowStore(
    var items: MutableMap<String, LegacyCoachingWindow>,
    var order: MutableList<String>,
) : LegacyCoachingWindowStore {
    override fun find(code: String): LegacyCoachingWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCoachingWindow) {
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

    override fun list(): MutableList<LegacyCoachingWindow> {
        val out = mutableListOf<LegacyCoachingWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCoachingWindowStore(): LegacyCoachingWindowStore {
    return MemoryLegacyCoachingWindowStore(
        mutableMapOf<String, LegacyCoachingWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCoachingWindowStore(
    var inner: LegacyCoachingWindowStore,
    var cache: TtlCache<LegacyCoachingWindow>,
    var hits: Int,
    var misses: Int,
) : LegacyCoachingWindowStore {
    override fun find(code: String): LegacyCoachingWindow? {
        val hit = (cache.get(code) as LegacyCoachingWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCoachingWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCoachingWindow> = inner.list()
}

fun newCachedLegacyCoachingWindowStore(inner: LegacyCoachingWindowStore, ttl: Int, clock: Clock): LegacyCoachingWindowStore {
    return CachedLegacyCoachingWindowStore(
        inner = inner,
        cache = newTtlCache<LegacyCoachingWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy coaching windows. Empty fields match all. */
data class LegacyCoachingWindowQuery(
    var source: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCoachingWindow): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyCoachingWindow>): MutableList<LegacyCoachingWindow> {
        val matched = filter(items) { candidate: LegacyCoachingWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCoachingWindows(store: LegacyCoachingWindowStore, query: LegacyCoachingWindowQuery): MutableList<LegacyCoachingWindow> {
    return query.apply(store.list())
}
