package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary scorecard windows by code. */
interface PrimaryScorecardWindowStore {
    fun find(code: String): PrimaryScorecardWindow?
    fun save(item: PrimaryScorecardWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryScorecardWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryScorecardWindowStore(
    var items: MutableMap<String, PrimaryScorecardWindow>,
    var order: MutableList<String>,
) : PrimaryScorecardWindowStore {
    override fun find(code: String): PrimaryScorecardWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryScorecardWindow) {
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

    override fun list(): MutableList<PrimaryScorecardWindow> {
        val out = mutableListOf<PrimaryScorecardWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryScorecardWindowStore(): PrimaryScorecardWindowStore {
    return MemoryPrimaryScorecardWindowStore(
        mutableMapOf<String, PrimaryScorecardWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryScorecardWindowStore(
    var inner: PrimaryScorecardWindowStore,
    var cache: TtlCache<PrimaryScorecardWindow>,
    var hits: Int,
    var misses: Int,
) : PrimaryScorecardWindowStore {
    override fun find(code: String): PrimaryScorecardWindow? {
        val hit = (cache.get(code) as PrimaryScorecardWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryScorecardWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryScorecardWindow> = inner.list()
}

fun newCachedPrimaryScorecardWindowStore(inner: PrimaryScorecardWindowStore, ttl: Int, clock: Clock): PrimaryScorecardWindowStore {
    return CachedPrimaryScorecardWindowStore(
        inner = inner,
        cache = newTtlCache<PrimaryScorecardWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary scorecard windows. Empty fields match all. */
data class PrimaryScorecardWindowQuery(
    var category: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryScorecardWindow): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryScorecardWindow>): MutableList<PrimaryScorecardWindow> {
        val matched = filter(items) { candidate: PrimaryScorecardWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryScorecardWindows(store: PrimaryScorecardWindowStore, query: PrimaryScorecardWindowQuery): MutableList<PrimaryScorecardWindow> {
    return query.apply(store.list())
}
