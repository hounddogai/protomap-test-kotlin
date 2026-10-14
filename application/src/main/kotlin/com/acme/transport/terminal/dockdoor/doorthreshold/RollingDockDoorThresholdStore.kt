package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling dock door thresholds by code. */
interface RollingDockDoorThresholdStore {
    fun find(code: String): RollingDockDoorThreshold?
    fun save(item: RollingDockDoorThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDockDoorThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDockDoorThresholdStore(
    var items: MutableMap<String, RollingDockDoorThreshold>,
    var order: MutableList<String>,
) : RollingDockDoorThresholdStore {
    override fun find(code: String): RollingDockDoorThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDockDoorThreshold) {
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

    override fun list(): MutableList<RollingDockDoorThreshold> {
        val out = mutableListOf<RollingDockDoorThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDockDoorThresholdStore(): RollingDockDoorThresholdStore {
    return MemoryRollingDockDoorThresholdStore(
        mutableMapOf<String, RollingDockDoorThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDockDoorThresholdStore(
    var inner: RollingDockDoorThresholdStore,
    var cache: TtlCache<RollingDockDoorThreshold>,
    var hits: Int,
    var misses: Int,
) : RollingDockDoorThresholdStore {
    override fun find(code: String): RollingDockDoorThreshold? {
        val hit = (cache.get(code) as RollingDockDoorThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDockDoorThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDockDoorThreshold> = inner.list()
}

fun newCachedRollingDockDoorThresholdStore(inner: RollingDockDoorThresholdStore, ttl: Int, clock: Clock): RollingDockDoorThresholdStore {
    return CachedRollingDockDoorThresholdStore(
        inner = inner,
        cache = newTtlCache<RollingDockDoorThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dock door thresholds. Empty fields match all. */
data class RollingDockDoorThresholdQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDockDoorThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDockDoorThreshold>): MutableList<RollingDockDoorThreshold> {
        val matched = filter(items) { candidate: RollingDockDoorThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDockDoorThresholds(store: RollingDockDoorThresholdStore, query: RollingDockDoorThresholdQuery): MutableList<RollingDockDoorThreshold> {
    return query.apply(store.list())
}
