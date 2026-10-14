package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient service area windows by code. */
interface LenientServiceAreaWindowStore {
    fun find(code: String): LenientServiceAreaWindow?
    fun save(item: LenientServiceAreaWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientServiceAreaWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientServiceAreaWindowStore(
    var items: MutableMap<String, LenientServiceAreaWindow>,
    var order: MutableList<String>,
) : LenientServiceAreaWindowStore {
    override fun find(code: String): LenientServiceAreaWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientServiceAreaWindow) {
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

    override fun list(): MutableList<LenientServiceAreaWindow> {
        val out = mutableListOf<LenientServiceAreaWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientServiceAreaWindowStore(): LenientServiceAreaWindowStore {
    return MemoryLenientServiceAreaWindowStore(
        mutableMapOf<String, LenientServiceAreaWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientServiceAreaWindowStore(
    var inner: LenientServiceAreaWindowStore,
    var cache: TtlCache<LenientServiceAreaWindow>,
    var hits: Int,
    var misses: Int,
) : LenientServiceAreaWindowStore {
    override fun find(code: String): LenientServiceAreaWindow? {
        val hit = (cache.get(code) as LenientServiceAreaWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientServiceAreaWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientServiceAreaWindow> = inner.list()
}

fun newCachedLenientServiceAreaWindowStore(inner: LenientServiceAreaWindowStore, ttl: Int, clock: Clock): LenientServiceAreaWindowStore {
    return CachedLenientServiceAreaWindowStore(
        inner = inner,
        cache = newTtlCache<LenientServiceAreaWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient service area windows. Empty fields match all. */
data class LenientServiceAreaWindowQuery(
    var country: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientServiceAreaWindow): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientServiceAreaWindow>): MutableList<LenientServiceAreaWindow> {
        val matched = filter(items) { candidate: LenientServiceAreaWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientServiceAreaWindows(store: LenientServiceAreaWindowStore, query: LenientServiceAreaWindowQuery): MutableList<LenientServiceAreaWindow> {
    return query.apply(store.list())
}
