package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient sortation windows by code. */
interface LenientSortationWindowStore {
    fun find(code: String): LenientSortationWindow?
    fun save(item: LenientSortationWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientSortationWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientSortationWindowStore(
    var items: MutableMap<String, LenientSortationWindow>,
    var order: MutableList<String>,
) : LenientSortationWindowStore {
    override fun find(code: String): LenientSortationWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientSortationWindow) {
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

    override fun list(): MutableList<LenientSortationWindow> {
        val out = mutableListOf<LenientSortationWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientSortationWindowStore(): LenientSortationWindowStore {
    return MemoryLenientSortationWindowStore(
        mutableMapOf<String, LenientSortationWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientSortationWindowStore(
    var inner: LenientSortationWindowStore,
    var cache: TtlCache<LenientSortationWindow>,
    var hits: Int,
    var misses: Int,
) : LenientSortationWindowStore {
    override fun find(code: String): LenientSortationWindow? {
        val hit = (cache.get(code) as LenientSortationWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientSortationWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientSortationWindow> = inner.list()
}

fun newCachedLenientSortationWindowStore(inner: LenientSortationWindowStore, ttl: Int, clock: Clock): LenientSortationWindowStore {
    return CachedLenientSortationWindowStore(
        inner = inner,
        cache = newTtlCache<LenientSortationWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient sortation windows. Empty fields match all. */
data class LenientSortationWindowQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientSortationWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientSortationWindow>): MutableList<LenientSortationWindow> {
        val matched = filter(items) { candidate: LenientSortationWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientSortationWindows(store: LenientSortationWindowStore, query: LenientSortationWindowQuery): MutableList<LenientSortationWindow> {
    return query.apply(store.list())
}
