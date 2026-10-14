package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient ev charge windows by code. */
interface LenientEvChargeWindowStore {
    fun find(code: String): LenientEvChargeWindow?
    fun save(item: LenientEvChargeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientEvChargeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientEvChargeWindowStore(
    var items: MutableMap<String, LenientEvChargeWindow>,
    var order: MutableList<String>,
) : LenientEvChargeWindowStore {
    override fun find(code: String): LenientEvChargeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientEvChargeWindow) {
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

    override fun list(): MutableList<LenientEvChargeWindow> {
        val out = mutableListOf<LenientEvChargeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientEvChargeWindowStore(): LenientEvChargeWindowStore {
    return MemoryLenientEvChargeWindowStore(
        mutableMapOf<String, LenientEvChargeWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientEvChargeWindowStore(
    var inner: LenientEvChargeWindowStore,
    var cache: TtlCache<LenientEvChargeWindow>,
    var hits: Int,
    var misses: Int,
) : LenientEvChargeWindowStore {
    override fun find(code: String): LenientEvChargeWindow? {
        val hit = (cache.get(code) as LenientEvChargeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientEvChargeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientEvChargeWindow> = inner.list()
}

fun newCachedLenientEvChargeWindowStore(inner: LenientEvChargeWindowStore, ttl: Int, clock: Clock): LenientEvChargeWindowStore {
    return CachedLenientEvChargeWindowStore(
        inner = inner,
        cache = newTtlCache<LenientEvChargeWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient ev charge windows. Empty fields match all. */
data class LenientEvChargeWindowQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientEvChargeWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientEvChargeWindow>): MutableList<LenientEvChargeWindow> {
        val matched = filter(items) { candidate: LenientEvChargeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientEvChargeWindows(store: LenientEvChargeWindowStore, query: LenientEvChargeWindowQuery): MutableList<LenientEvChargeWindow> {
    return query.apply(store.list())
}
