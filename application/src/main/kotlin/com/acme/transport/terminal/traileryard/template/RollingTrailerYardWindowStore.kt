package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling trailer yard windows by code. */
interface RollingTrailerYardWindowStore {
    fun find(code: String): RollingTrailerYardWindow?
    fun save(item: RollingTrailerYardWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTrailerYardWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTrailerYardWindowStore(
    var items: MutableMap<String, RollingTrailerYardWindow>,
    var order: MutableList<String>,
) : RollingTrailerYardWindowStore {
    override fun find(code: String): RollingTrailerYardWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTrailerYardWindow) {
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

    override fun list(): MutableList<RollingTrailerYardWindow> {
        val out = mutableListOf<RollingTrailerYardWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTrailerYardWindowStore(): RollingTrailerYardWindowStore {
    return MemoryRollingTrailerYardWindowStore(
        mutableMapOf<String, RollingTrailerYardWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTrailerYardWindowStore(
    var inner: RollingTrailerYardWindowStore,
    var cache: TtlCache<RollingTrailerYardWindow>,
    var hits: Int,
    var misses: Int,
) : RollingTrailerYardWindowStore {
    override fun find(code: String): RollingTrailerYardWindow? {
        val hit = (cache.get(code) as RollingTrailerYardWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTrailerYardWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTrailerYardWindow> = inner.list()
}

fun newCachedRollingTrailerYardWindowStore(inner: RollingTrailerYardWindowStore, ttl: Int, clock: Clock): RollingTrailerYardWindowStore {
    return CachedRollingTrailerYardWindowStore(
        inner = inner,
        cache = newTtlCache<RollingTrailerYardWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling trailer yard windows. Empty fields match all. */
data class RollingTrailerYardWindowQuery(
    var channel: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTrailerYardWindow): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTrailerYardWindow>): MutableList<RollingTrailerYardWindow> {
        val matched = filter(items) { candidate: RollingTrailerYardWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTrailerYardWindows(store: RollingTrailerYardWindowStore, query: RollingTrailerYardWindowQuery): MutableList<RollingTrailerYardWindow> {
    return query.apply(store.list())
}
