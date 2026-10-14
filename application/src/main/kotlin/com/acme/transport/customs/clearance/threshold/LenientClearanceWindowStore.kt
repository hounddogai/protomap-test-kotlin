package com.acme.transport.customs.clearance.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient clearance windows by code. */
interface LenientClearanceWindowStore {
    fun find(code: String): LenientClearanceWindow?
    fun save(item: LenientClearanceWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientClearanceWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientClearanceWindowStore(
    var items: MutableMap<String, LenientClearanceWindow>,
    var order: MutableList<String>,
) : LenientClearanceWindowStore {
    override fun find(code: String): LenientClearanceWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientClearanceWindow) {
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

    override fun list(): MutableList<LenientClearanceWindow> {
        val out = mutableListOf<LenientClearanceWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientClearanceWindowStore(): LenientClearanceWindowStore {
    return MemoryLenientClearanceWindowStore(
        mutableMapOf<String, LenientClearanceWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientClearanceWindowStore(
    var inner: LenientClearanceWindowStore,
    var cache: TtlCache<LenientClearanceWindow>,
    var hits: Int,
    var misses: Int,
) : LenientClearanceWindowStore {
    override fun find(code: String): LenientClearanceWindow? {
        val hit = (cache.get(code) as LenientClearanceWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientClearanceWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientClearanceWindow> = inner.list()
}

fun newCachedLenientClearanceWindowStore(inner: LenientClearanceWindowStore, ttl: Int, clock: Clock): LenientClearanceWindowStore {
    return CachedLenientClearanceWindowStore(
        inner = inner,
        cache = newTtlCache<LenientClearanceWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient clearance windows. Empty fields match all. */
data class LenientClearanceWindowQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientClearanceWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientClearanceWindow>): MutableList<LenientClearanceWindow> {
        val matched = filter(items) { candidate: LenientClearanceWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientClearanceWindows(store: LenientClearanceWindowStore, query: LenientClearanceWindowQuery): MutableList<LenientClearanceWindow> {
    return query.apply(store.list())
}
