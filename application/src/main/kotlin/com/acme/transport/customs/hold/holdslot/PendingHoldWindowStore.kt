package com.acme.transport.customs.hold.holdslot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending hold windows by code. */
interface PendingHoldWindowStore {
    fun find(code: String): PendingHoldWindow?
    fun save(item: PendingHoldWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingHoldWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingHoldWindowStore(
    var items: MutableMap<String, PendingHoldWindow>,
    var order: MutableList<String>,
) : PendingHoldWindowStore {
    override fun find(code: String): PendingHoldWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingHoldWindow) {
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

    override fun list(): MutableList<PendingHoldWindow> {
        val out = mutableListOf<PendingHoldWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingHoldWindowStore(): PendingHoldWindowStore {
    return MemoryPendingHoldWindowStore(mutableMapOf<String, PendingHoldWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingHoldWindowStore(
    var inner: PendingHoldWindowStore,
    var cache: TtlCache<PendingHoldWindow>,
    var hits: Int,
    var misses: Int,
) : PendingHoldWindowStore {
    override fun find(code: String): PendingHoldWindow? {
        val hit = (cache.get(code) as PendingHoldWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingHoldWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingHoldWindow> = inner.list()
}

fun newCachedPendingHoldWindowStore(inner: PendingHoldWindowStore, ttl: Int, clock: Clock): PendingHoldWindowStore {
    return CachedPendingHoldWindowStore(
        inner = inner,
        cache = newTtlCache<PendingHoldWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending hold windows. Empty fields match all. */
data class PendingHoldWindowQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingHoldWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingHoldWindow>): MutableList<PendingHoldWindow> {
        val matched = filter(items) { candidate: PendingHoldWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingHoldWindows(store: PendingHoldWindowStore, query: PendingHoldWindowQuery): MutableList<PendingHoldWindow> {
    return query.apply(store.list())
}
