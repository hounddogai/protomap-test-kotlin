package com.acme.transport.lastmile.locker.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker windows by code. */
interface LockerWindowStore {
    fun find(code: String): LockerWindow?
    fun save(item: LockerWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerWindowStore(
    var items: MutableMap<String, LockerWindow>,
    var order: MutableList<String>,
) : LockerWindowStore {
    override fun find(code: String): LockerWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerWindow) {
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

    override fun list(): MutableList<LockerWindow> {
        val out = mutableListOf<LockerWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerWindowStore(): LockerWindowStore {
    return MemoryLockerWindowStore(mutableMapOf<String, LockerWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerWindowStore(
    var inner: LockerWindowStore,
    var cache: TtlCache<LockerWindow>,
    var hits: Int,
    var misses: Int,
) : LockerWindowStore {
    override fun find(code: String): LockerWindow? {
        val hit = (cache.get(code) as LockerWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerWindow> = inner.list()
}

fun newCachedLockerWindowStore(inner: LockerWindowStore, ttl: Int, clock: Clock): LockerWindowStore {
    return CachedLockerWindowStore(
        inner = inner,
        cache = newTtlCache<LockerWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker windows. Empty fields match all. */
data class LockerWindowQuery(
    var category: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerWindow): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerWindow>): MutableList<LockerWindow> {
        val matched = filter(items) { candidate: LockerWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerWindows(store: LockerWindowStore, query: LockerWindowQuery): MutableList<LockerWindow> {
    return query.apply(store.list())
}
