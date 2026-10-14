package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient scorecard windows by code. */
interface LenientScorecardWindowStore {
    fun find(code: String): LenientScorecardWindow?
    fun save(item: LenientScorecardWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientScorecardWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientScorecardWindowStore(
    var items: MutableMap<String, LenientScorecardWindow>,
    var order: MutableList<String>,
) : LenientScorecardWindowStore {
    override fun find(code: String): LenientScorecardWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientScorecardWindow) {
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

    override fun list(): MutableList<LenientScorecardWindow> {
        val out = mutableListOf<LenientScorecardWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientScorecardWindowStore(): LenientScorecardWindowStore {
    return MemoryLenientScorecardWindowStore(
        mutableMapOf<String, LenientScorecardWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientScorecardWindowStore(
    var inner: LenientScorecardWindowStore,
    var cache: TtlCache<LenientScorecardWindow>,
    var hits: Int,
    var misses: Int,
) : LenientScorecardWindowStore {
    override fun find(code: String): LenientScorecardWindow? {
        val hit = (cache.get(code) as LenientScorecardWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientScorecardWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientScorecardWindow> = inner.list()
}

fun newCachedLenientScorecardWindowStore(inner: LenientScorecardWindowStore, ttl: Int, clock: Clock): LenientScorecardWindowStore {
    return CachedLenientScorecardWindowStore(
        inner = inner,
        cache = newTtlCache<LenientScorecardWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient scorecard windows. Empty fields match all. */
data class LenientScorecardWindowQuery(
    var segment: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientScorecardWindow): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<LenientScorecardWindow>): MutableList<LenientScorecardWindow> {
        val matched = filter(items) { candidate: LenientScorecardWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientScorecardWindows(store: LenientScorecardWindowStore, query: LenientScorecardWindowQuery): MutableList<LenientScorecardWindow> {
    return query.apply(store.list())
}
