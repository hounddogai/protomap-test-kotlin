package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling sortation windows by code. */
interface RollingSortationWindowStore {
    fun find(code: String): RollingSortationWindow?
    fun save(item: RollingSortationWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingSortationWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingSortationWindowStore(
    var items: MutableMap<String, RollingSortationWindow>,
    var order: MutableList<String>,
) : RollingSortationWindowStore {
    override fun find(code: String): RollingSortationWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingSortationWindow) {
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

    override fun list(): MutableList<RollingSortationWindow> {
        val out = mutableListOf<RollingSortationWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingSortationWindowStore(): RollingSortationWindowStore {
    return MemoryRollingSortationWindowStore(
        mutableMapOf<String, RollingSortationWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingSortationWindowStore(
    var inner: RollingSortationWindowStore,
    var cache: TtlCache<RollingSortationWindow>,
    var hits: Int,
    var misses: Int,
) : RollingSortationWindowStore {
    override fun find(code: String): RollingSortationWindow? {
        val hit = (cache.get(code) as RollingSortationWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingSortationWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingSortationWindow> = inner.list()
}

fun newCachedRollingSortationWindowStore(inner: RollingSortationWindowStore, ttl: Int, clock: Clock): RollingSortationWindowStore {
    return CachedRollingSortationWindowStore(
        inner = inner,
        cache = newTtlCache<RollingSortationWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling sortation windows. Empty fields match all. */
data class RollingSortationWindowQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingSortationWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RollingSortationWindow>): MutableList<RollingSortationWindow> {
        val matched = filter(items) { candidate: RollingSortationWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingSortationWindows(store: RollingSortationWindowStore, query: RollingSortationWindowQuery): MutableList<RollingSortationWindow> {
    return query.apply(store.list())
}
