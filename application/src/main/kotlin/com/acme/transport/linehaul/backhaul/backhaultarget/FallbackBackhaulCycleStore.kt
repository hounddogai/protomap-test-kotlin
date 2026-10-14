package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback backhaul cycles by code. */
interface FallbackBackhaulCycleStore {
    fun find(code: String): FallbackBackhaulCycle?
    fun save(item: FallbackBackhaulCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackBackhaulCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackBackhaulCycleStore(
    var items: MutableMap<String, FallbackBackhaulCycle>,
    var order: MutableList<String>,
) : FallbackBackhaulCycleStore {
    override fun find(code: String): FallbackBackhaulCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackBackhaulCycle) {
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

    override fun list(): MutableList<FallbackBackhaulCycle> {
        val out = mutableListOf<FallbackBackhaulCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackBackhaulCycleStore(): FallbackBackhaulCycleStore {
    return MemoryFallbackBackhaulCycleStore(
        mutableMapOf<String, FallbackBackhaulCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackBackhaulCycleStore(
    var inner: FallbackBackhaulCycleStore,
    var cache: TtlCache<FallbackBackhaulCycle>,
    var hits: Int,
    var misses: Int,
) : FallbackBackhaulCycleStore {
    override fun find(code: String): FallbackBackhaulCycle? {
        val hit = (cache.get(code) as FallbackBackhaulCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackBackhaulCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackBackhaulCycle> = inner.list()
}

fun newCachedFallbackBackhaulCycleStore(inner: FallbackBackhaulCycleStore, ttl: Int, clock: Clock): FallbackBackhaulCycleStore {
    return CachedFallbackBackhaulCycleStore(
        inner = inner,
        cache = newTtlCache<FallbackBackhaulCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback backhaul cycles. Empty fields match all. */
data class FallbackBackhaulCycleQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackBackhaulCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackBackhaulCycle>): MutableList<FallbackBackhaulCycle> {
        val matched = filter(items) { candidate: FallbackBackhaulCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackBackhaulCycles(store: FallbackBackhaulCycleStore, query: FallbackBackhaulCycleQuery): MutableList<FallbackBackhaulCycle> {
    return query.apply(store.list())
}
