package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door targets by code. */
interface DockDoorTargetStore {
    fun find(code: String): DockDoorTarget?
    fun save(item: DockDoorTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorTargetStore(
    var items: MutableMap<String, DockDoorTarget>,
    var order: MutableList<String>,
) : DockDoorTargetStore {
    override fun find(code: String): DockDoorTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorTarget) {
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

    override fun list(): MutableList<DockDoorTarget> {
        val out = mutableListOf<DockDoorTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorTargetStore(): DockDoorTargetStore {
    return MemoryDockDoorTargetStore(mutableMapOf<String, DockDoorTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorTargetStore(
    var inner: DockDoorTargetStore,
    var cache: TtlCache<DockDoorTarget>,
    var hits: Int,
    var misses: Int,
) : DockDoorTargetStore {
    override fun find(code: String): DockDoorTarget? {
        val hit = (cache.get(code) as DockDoorTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorTarget> = inner.list()
}

fun newCachedDockDoorTargetStore(inner: DockDoorTargetStore, ttl: Int, clock: Clock): DockDoorTargetStore {
    return CachedDockDoorTargetStore(
        inner = inner,
        cache = newTtlCache<DockDoorTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door targets. Empty fields match all. */
data class DockDoorTargetQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorTarget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorTarget>): MutableList<DockDoorTarget> {
        val matched = filter(items) { candidate: DockDoorTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorTargets(store: DockDoorTargetStore, query: DockDoorTargetQuery): MutableList<DockDoorTarget> {
    return query.apply(store.list())
}
