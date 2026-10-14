package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback dock door groups by code. */
interface FallbackDockDoorGroupStore {
    fun find(code: String): FallbackDockDoorGroup?
    fun save(item: FallbackDockDoorGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDockDoorGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDockDoorGroupStore(
    var items: MutableMap<String, FallbackDockDoorGroup>,
    var order: MutableList<String>,
) : FallbackDockDoorGroupStore {
    override fun find(code: String): FallbackDockDoorGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDockDoorGroup) {
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

    override fun list(): MutableList<FallbackDockDoorGroup> {
        val out = mutableListOf<FallbackDockDoorGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDockDoorGroupStore(): FallbackDockDoorGroupStore {
    return MemoryFallbackDockDoorGroupStore(
        mutableMapOf<String, FallbackDockDoorGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDockDoorGroupStore(
    var inner: FallbackDockDoorGroupStore,
    var cache: TtlCache<FallbackDockDoorGroup>,
    var hits: Int,
    var misses: Int,
) : FallbackDockDoorGroupStore {
    override fun find(code: String): FallbackDockDoorGroup? {
        val hit = (cache.get(code) as FallbackDockDoorGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDockDoorGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDockDoorGroup> = inner.list()
}

fun newCachedFallbackDockDoorGroupStore(inner: FallbackDockDoorGroupStore, ttl: Int, clock: Clock): FallbackDockDoorGroupStore {
    return CachedFallbackDockDoorGroupStore(
        inner = inner,
        cache = newTtlCache<FallbackDockDoorGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dock door groups. Empty fields match all. */
data class FallbackDockDoorGroupQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDockDoorGroup): Boolean {
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

    fun apply(items: MutableList<FallbackDockDoorGroup>): MutableList<FallbackDockDoorGroup> {
        val matched = filter(items) { candidate: FallbackDockDoorGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDockDoorGroups(store: FallbackDockDoorGroupStore, query: FallbackDockDoorGroupQuery): MutableList<FallbackDockDoorGroup> {
    return query.apply(store.list())
}
