package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer windows by code. */
interface TrailerWindowStore {
    fun find(code: String): TrailerWindow?
    fun save(item: TrailerWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerWindowStore(
    var items: MutableMap<String, TrailerWindow>,
    var order: MutableList<String>,
) : TrailerWindowStore {
    override fun find(code: String): TrailerWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerWindow) {
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

    override fun list(): MutableList<TrailerWindow> {
        val out = mutableListOf<TrailerWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerWindowStore(): TrailerWindowStore {
    return MemoryTrailerWindowStore(mutableMapOf<String, TrailerWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerWindowStore(
    var inner: TrailerWindowStore,
    var cache: TtlCache<TrailerWindow>,
    var hits: Int,
    var misses: Int,
) : TrailerWindowStore {
    override fun find(code: String): TrailerWindow? {
        val hit = (cache.get(code) as TrailerWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerWindow> = inner.list()
}

fun newCachedTrailerWindowStore(inner: TrailerWindowStore, ttl: Int, clock: Clock): TrailerWindowStore {
    return CachedTrailerWindowStore(
        inner = inner,
        cache = newTtlCache<TrailerWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer windows. Empty fields match all. */
data class TrailerWindowQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerWindow>): MutableList<TrailerWindow> {
        val matched = filter(items) { candidate: TrailerWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerWindows(store: TrailerWindowStore, query: TrailerWindowQuery): MutableList<TrailerWindow> {
    return query.apply(store.list())
}
