package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending backhaul cycles by code. */
interface PendingBackhaulCycleStore {
    fun find(code: String): PendingBackhaulCycle?
    fun save(item: PendingBackhaulCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingBackhaulCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingBackhaulCycleStore(
    var items: MutableMap<String, PendingBackhaulCycle>,
    var order: MutableList<String>,
) : PendingBackhaulCycleStore {
    override fun find(code: String): PendingBackhaulCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingBackhaulCycle) {
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

    override fun list(): MutableList<PendingBackhaulCycle> {
        val out = mutableListOf<PendingBackhaulCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingBackhaulCycleStore(): PendingBackhaulCycleStore {
    return MemoryPendingBackhaulCycleStore(
        mutableMapOf<String, PendingBackhaulCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingBackhaulCycleStore(
    var inner: PendingBackhaulCycleStore,
    var cache: TtlCache<PendingBackhaulCycle>,
    var hits: Int,
    var misses: Int,
) : PendingBackhaulCycleStore {
    override fun find(code: String): PendingBackhaulCycle? {
        val hit = (cache.get(code) as PendingBackhaulCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingBackhaulCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingBackhaulCycle> = inner.list()
}

fun newCachedPendingBackhaulCycleStore(inner: PendingBackhaulCycleStore, ttl: Int, clock: Clock): PendingBackhaulCycleStore {
    return CachedPendingBackhaulCycleStore(
        inner = inner,
        cache = newTtlCache<PendingBackhaulCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending backhaul cycles. Empty fields match all. */
data class PendingBackhaulCycleQuery(
    var locale: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingBackhaulCycle): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingBackhaulCycle>): MutableList<PendingBackhaulCycle> {
        val matched = filter(items) { candidate: PendingBackhaulCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingBackhaulCycles(store: PendingBackhaulCycleStore, query: PendingBackhaulCycleQuery): MutableList<PendingBackhaulCycle> {
    return query.apply(store.list())
}
