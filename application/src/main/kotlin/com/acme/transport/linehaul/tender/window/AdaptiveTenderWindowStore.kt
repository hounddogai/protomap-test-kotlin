package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive tender windows by code. */
interface AdaptiveTenderWindowStore {
    fun find(code: String): AdaptiveTenderWindow?
    fun save(item: AdaptiveTenderWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTenderWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTenderWindowStore(
    var items: MutableMap<String, AdaptiveTenderWindow>,
    var order: MutableList<String>,
) : AdaptiveTenderWindowStore {
    override fun find(code: String): AdaptiveTenderWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTenderWindow) {
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

    override fun list(): MutableList<AdaptiveTenderWindow> {
        val out = mutableListOf<AdaptiveTenderWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTenderWindowStore(): AdaptiveTenderWindowStore {
    return MemoryAdaptiveTenderWindowStore(
        mutableMapOf<String, AdaptiveTenderWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTenderWindowStore(
    var inner: AdaptiveTenderWindowStore,
    var cache: TtlCache<AdaptiveTenderWindow>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTenderWindowStore {
    override fun find(code: String): AdaptiveTenderWindow? {
        val hit = (cache.get(code) as AdaptiveTenderWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTenderWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTenderWindow> = inner.list()
}

fun newCachedAdaptiveTenderWindowStore(inner: AdaptiveTenderWindowStore, ttl: Int, clock: Clock): AdaptiveTenderWindowStore {
    return CachedAdaptiveTenderWindowStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTenderWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive tender windows. Empty fields match all. */
data class AdaptiveTenderWindowQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTenderWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTenderWindow>): MutableList<AdaptiveTenderWindow> {
        val matched = filter(items) { candidate: AdaptiveTenderWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTenderWindows(store: AdaptiveTenderWindowStore, query: AdaptiveTenderWindowQuery): MutableList<AdaptiveTenderWindow> {
    return query.apply(store.list())
}
