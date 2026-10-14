package com.acme.transport.terminal.dwell.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell windows by code. */
interface DwellWindowStore {
    fun find(code: String): DwellWindow?
    fun save(item: DwellWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellWindowStore(
    var items: MutableMap<String, DwellWindow>,
    var order: MutableList<String>,
) : DwellWindowStore {
    override fun find(code: String): DwellWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellWindow) {
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

    override fun list(): MutableList<DwellWindow> {
        val out = mutableListOf<DwellWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellWindowStore(): DwellWindowStore {
    return MemoryDwellWindowStore(mutableMapOf<String, DwellWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellWindowStore(
    var inner: DwellWindowStore,
    var cache: TtlCache<DwellWindow>,
    var hits: Int,
    var misses: Int,
) : DwellWindowStore {
    override fun find(code: String): DwellWindow? {
        val hit = (cache.get(code) as DwellWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellWindow> = inner.list()
}

fun newCachedDwellWindowStore(inner: DwellWindowStore, ttl: Int, clock: Clock): DwellWindowStore {
    return CachedDwellWindowStore(
        inner = inner,
        cache = newTtlCache<DwellWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell windows. Empty fields match all. */
data class DwellWindowQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellWindow): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellWindow>): MutableList<DwellWindow> {
        val matched = filter(items) { candidate: DwellWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellWindows(store: DwellWindowStore, query: DwellWindowQuery): MutableList<DwellWindow> {
    return query.apply(store.list())
}
