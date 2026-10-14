package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual hold windows by code. */
interface ManualHoldWindowStore {
    fun find(code: String): ManualHoldWindow?
    fun save(item: ManualHoldWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualHoldWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualHoldWindowStore(
    var items: MutableMap<String, ManualHoldWindow>,
    var order: MutableList<String>,
) : ManualHoldWindowStore {
    override fun find(code: String): ManualHoldWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualHoldWindow) {
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

    override fun list(): MutableList<ManualHoldWindow> {
        val out = mutableListOf<ManualHoldWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualHoldWindowStore(): ManualHoldWindowStore {
    return MemoryManualHoldWindowStore(mutableMapOf<String, ManualHoldWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualHoldWindowStore(
    var inner: ManualHoldWindowStore,
    var cache: TtlCache<ManualHoldWindow>,
    var hits: Int,
    var misses: Int,
) : ManualHoldWindowStore {
    override fun find(code: String): ManualHoldWindow? {
        val hit = (cache.get(code) as ManualHoldWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualHoldWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualHoldWindow> = inner.list()
}

fun newCachedManualHoldWindowStore(inner: ManualHoldWindowStore, ttl: Int, clock: Clock): ManualHoldWindowStore {
    return CachedManualHoldWindowStore(
        inner = inner,
        cache = newTtlCache<ManualHoldWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual hold windows. Empty fields match all. */
data class ManualHoldWindowQuery(
    var country: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualHoldWindow): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualHoldWindow>): MutableList<ManualHoldWindow> {
        val matched = filter(items) { candidate: ManualHoldWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualHoldWindows(store: ManualHoldWindowStore, query: ManualHoldWindowQuery): MutableList<ManualHoldWindow> {
    return query.apply(store.list())
}
