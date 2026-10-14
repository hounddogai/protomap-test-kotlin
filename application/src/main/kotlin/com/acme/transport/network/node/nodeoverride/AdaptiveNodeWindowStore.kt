package com.acme.transport.network.node.nodeoverride

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive node windows by code. */
interface AdaptiveNodeWindowStore {
    fun find(code: String): AdaptiveNodeWindow?
    fun save(item: AdaptiveNodeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveNodeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveNodeWindowStore(
    var items: MutableMap<String, AdaptiveNodeWindow>,
    var order: MutableList<String>,
) : AdaptiveNodeWindowStore {
    override fun find(code: String): AdaptiveNodeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveNodeWindow) {
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

    override fun list(): MutableList<AdaptiveNodeWindow> {
        val out = mutableListOf<AdaptiveNodeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveNodeWindowStore(): AdaptiveNodeWindowStore {
    return MemoryAdaptiveNodeWindowStore(mutableMapOf<String, AdaptiveNodeWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveNodeWindowStore(
    var inner: AdaptiveNodeWindowStore,
    var cache: TtlCache<AdaptiveNodeWindow>,
    var hits: Int,
    var misses: Int,
) : AdaptiveNodeWindowStore {
    override fun find(code: String): AdaptiveNodeWindow? {
        val hit = (cache.get(code) as AdaptiveNodeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveNodeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveNodeWindow> = inner.list()
}

fun newCachedAdaptiveNodeWindowStore(inner: AdaptiveNodeWindowStore, ttl: Int, clock: Clock): AdaptiveNodeWindowStore {
    return CachedAdaptiveNodeWindowStore(
        inner = inner,
        cache = newTtlCache<AdaptiveNodeWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive node windows. Empty fields match all. */
data class AdaptiveNodeWindowQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveNodeWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveNodeWindow>): MutableList<AdaptiveNodeWindow> {
        val matched = filter(items) { candidate: AdaptiveNodeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveNodeWindows(store: AdaptiveNodeWindowStore, query: AdaptiveNodeWindowQuery): MutableList<AdaptiveNodeWindow> {
    return query.apply(store.list())
}
