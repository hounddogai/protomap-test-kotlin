package com.acme.transport.safety.incident.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback incident windows by code. */
interface FallbackIncidentWindowStore {
    fun find(code: String): FallbackIncidentWindow?
    fun save(item: FallbackIncidentWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackIncidentWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackIncidentWindowStore(
    var items: MutableMap<String, FallbackIncidentWindow>,
    var order: MutableList<String>,
) : FallbackIncidentWindowStore {
    override fun find(code: String): FallbackIncidentWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackIncidentWindow) {
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

    override fun list(): MutableList<FallbackIncidentWindow> {
        val out = mutableListOf<FallbackIncidentWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackIncidentWindowStore(): FallbackIncidentWindowStore {
    return MemoryFallbackIncidentWindowStore(
        mutableMapOf<String, FallbackIncidentWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackIncidentWindowStore(
    var inner: FallbackIncidentWindowStore,
    var cache: TtlCache<FallbackIncidentWindow>,
    var hits: Int,
    var misses: Int,
) : FallbackIncidentWindowStore {
    override fun find(code: String): FallbackIncidentWindow? {
        val hit = (cache.get(code) as FallbackIncidentWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackIncidentWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackIncidentWindow> = inner.list()
}

fun newCachedFallbackIncidentWindowStore(inner: FallbackIncidentWindowStore, ttl: Int, clock: Clock): FallbackIncidentWindowStore {
    return CachedFallbackIncidentWindowStore(
        inner = inner,
        cache = newTtlCache<FallbackIncidentWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback incident windows. Empty fields match all. */
data class FallbackIncidentWindowQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackIncidentWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackIncidentWindow>): MutableList<FallbackIncidentWindow> {
        val matched = filter(items) { candidate: FallbackIncidentWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackIncidentWindows(store: FallbackIncidentWindowStore, query: FallbackIncidentWindowQuery): MutableList<FallbackIncidentWindow> {
    return query.apply(store.list())
}
