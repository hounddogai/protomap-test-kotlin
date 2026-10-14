package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered dock door targets by code. */
interface TieredDockDoorTargetStore {
    fun find(code: String): TieredDockDoorTarget?
    fun save(item: TieredDockDoorTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDockDoorTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDockDoorTargetStore(
    var items: MutableMap<String, TieredDockDoorTarget>,
    var order: MutableList<String>,
) : TieredDockDoorTargetStore {
    override fun find(code: String): TieredDockDoorTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDockDoorTarget) {
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

    override fun list(): MutableList<TieredDockDoorTarget> {
        val out = mutableListOf<TieredDockDoorTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDockDoorTargetStore(): TieredDockDoorTargetStore {
    return MemoryTieredDockDoorTargetStore(
        mutableMapOf<String, TieredDockDoorTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDockDoorTargetStore(
    var inner: TieredDockDoorTargetStore,
    var cache: TtlCache<TieredDockDoorTarget>,
    var hits: Int,
    var misses: Int,
) : TieredDockDoorTargetStore {
    override fun find(code: String): TieredDockDoorTarget? {
        val hit = (cache.get(code) as TieredDockDoorTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDockDoorTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDockDoorTarget> = inner.list()
}

fun newCachedTieredDockDoorTargetStore(inner: TieredDockDoorTargetStore, ttl: Int, clock: Clock): TieredDockDoorTargetStore {
    return CachedTieredDockDoorTargetStore(
        inner = inner,
        cache = newTtlCache<TieredDockDoorTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered dock door targets. Empty fields match all. */
data class TieredDockDoorTargetQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDockDoorTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDockDoorTarget>): MutableList<TieredDockDoorTarget> {
        val matched = filter(items) { candidate: TieredDockDoorTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDockDoorTargets(store: TieredDockDoorTargetStore, query: TieredDockDoorTargetQuery): MutableList<TieredDockDoorTarget> {
    return query.apply(store.list())
}
