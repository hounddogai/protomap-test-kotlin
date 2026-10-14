package com.acme.transport.emissions.idling.bundle

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped idling windows by code. */
interface CappedIdlingWindowStore {
    fun find(code: String): CappedIdlingWindow?
    fun save(item: CappedIdlingWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedIdlingWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedIdlingWindowStore(
    var items: MutableMap<String, CappedIdlingWindow>,
    var order: MutableList<String>,
) : CappedIdlingWindowStore {
    override fun find(code: String): CappedIdlingWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedIdlingWindow) {
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

    override fun list(): MutableList<CappedIdlingWindow> {
        val out = mutableListOf<CappedIdlingWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedIdlingWindowStore(): CappedIdlingWindowStore {
    return MemoryCappedIdlingWindowStore(mutableMapOf<String, CappedIdlingWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedIdlingWindowStore(
    var inner: CappedIdlingWindowStore,
    var cache: TtlCache<CappedIdlingWindow>,
    var hits: Int,
    var misses: Int,
) : CappedIdlingWindowStore {
    override fun find(code: String): CappedIdlingWindow? {
        val hit = (cache.get(code) as CappedIdlingWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedIdlingWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedIdlingWindow> = inner.list()
}

fun newCachedCappedIdlingWindowStore(inner: CappedIdlingWindowStore, ttl: Int, clock: Clock): CappedIdlingWindowStore {
    return CachedCappedIdlingWindowStore(
        inner = inner,
        cache = newTtlCache<CappedIdlingWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped idling windows. Empty fields match all. */
data class CappedIdlingWindowQuery(
    var lane: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedIdlingWindow): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedIdlingWindow>): MutableList<CappedIdlingWindow> {
        val matched = filter(items) { candidate: CappedIdlingWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedIdlingWindows(store: CappedIdlingWindowStore, query: CappedIdlingWindowQuery): MutableList<CappedIdlingWindow> {
    return query.apply(store.list())
}
