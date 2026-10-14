package com.acme.transport.freightpay.accessorial.variance

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial windows by code. */
interface AccessorialWindowStore {
    fun find(code: String): AccessorialWindow?
    fun save(item: AccessorialWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialWindowStore(
    var items: MutableMap<String, AccessorialWindow>,
    var order: MutableList<String>,
) : AccessorialWindowStore {
    override fun find(code: String): AccessorialWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialWindow) {
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

    override fun list(): MutableList<AccessorialWindow> {
        val out = mutableListOf<AccessorialWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialWindowStore(): AccessorialWindowStore {
    return MemoryAccessorialWindowStore(mutableMapOf<String, AccessorialWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialWindowStore(
    var inner: AccessorialWindowStore,
    var cache: TtlCache<AccessorialWindow>,
    var hits: Int,
    var misses: Int,
) : AccessorialWindowStore {
    override fun find(code: String): AccessorialWindow? {
        val hit = (cache.get(code) as AccessorialWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialWindow> = inner.list()
}

fun newCachedAccessorialWindowStore(inner: AccessorialWindowStore, ttl: Int, clock: Clock): AccessorialWindowStore {
    return CachedAccessorialWindowStore(
        inner = inner,
        cache = newTtlCache<AccessorialWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial windows. Empty fields match all. */
data class AccessorialWindowQuery(
    var source: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialWindow): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<AccessorialWindow>): MutableList<AccessorialWindow> {
        val matched = filter(items) { candidate: AccessorialWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialWindows(store: AccessorialWindowStore, query: AccessorialWindowQuery): MutableList<AccessorialWindow> {
    return query.apply(store.list())
}
