package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard windows by code. */
interface ScorecardWindowStore {
    fun find(code: String): ScorecardWindow?
    fun save(item: ScorecardWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardWindowStore(
    var items: MutableMap<String, ScorecardWindow>,
    var order: MutableList<String>,
) : ScorecardWindowStore {
    override fun find(code: String): ScorecardWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardWindow) {
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

    override fun list(): MutableList<ScorecardWindow> {
        val out = mutableListOf<ScorecardWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardWindowStore(): ScorecardWindowStore {
    return MemoryScorecardWindowStore(mutableMapOf<String, ScorecardWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardWindowStore(
    var inner: ScorecardWindowStore,
    var cache: TtlCache<ScorecardWindow>,
    var hits: Int,
    var misses: Int,
) : ScorecardWindowStore {
    override fun find(code: String): ScorecardWindow? {
        val hit = (cache.get(code) as ScorecardWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardWindow> = inner.list()
}

fun newCachedScorecardWindowStore(inner: ScorecardWindowStore, ttl: Int, clock: Clock): ScorecardWindowStore {
    return CachedScorecardWindowStore(
        inner = inner,
        cache = newTtlCache<ScorecardWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard windows. Empty fields match all. */
data class ScorecardWindowQuery(
    var region: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardWindow): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<ScorecardWindow>): MutableList<ScorecardWindow> {
        val matched = filter(items) { candidate: ScorecardWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardWindows(store: ScorecardWindowStore, query: ScorecardWindowQuery): MutableList<ScorecardWindow> {
    return query.apply(store.list())
}
