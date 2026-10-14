package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge windows by code. */
interface EvChargeWindowStore {
    fun find(code: String): EvChargeWindow?
    fun save(item: EvChargeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeWindowStore(
    var items: MutableMap<String, EvChargeWindow>,
    var order: MutableList<String>,
) : EvChargeWindowStore {
    override fun find(code: String): EvChargeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeWindow) {
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

    override fun list(): MutableList<EvChargeWindow> {
        val out = mutableListOf<EvChargeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeWindowStore(): EvChargeWindowStore {
    return MemoryEvChargeWindowStore(mutableMapOf<String, EvChargeWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeWindowStore(
    var inner: EvChargeWindowStore,
    var cache: TtlCache<EvChargeWindow>,
    var hits: Int,
    var misses: Int,
) : EvChargeWindowStore {
    override fun find(code: String): EvChargeWindow? {
        val hit = (cache.get(code) as EvChargeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeWindow> = inner.list()
}

fun newCachedEvChargeWindowStore(inner: EvChargeWindowStore, ttl: Int, clock: Clock): EvChargeWindowStore {
    return CachedEvChargeWindowStore(
        inner = inner,
        cache = newTtlCache<EvChargeWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge windows. Empty fields match all. */
data class EvChargeWindowQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeWindow>): MutableList<EvChargeWindow> {
        val matched = filter(items) { candidate: EvChargeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeWindows(store: EvChargeWindowStore, query: EvChargeWindowQuery): MutableList<EvChargeWindow> {
    return query.apply(store.list())
}
