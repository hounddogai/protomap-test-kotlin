package com.acme.transport.customs.hold.adjustment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold cycles by code. */
interface HoldCycleStore {
    fun find(code: String): HoldCycle?
    fun save(item: HoldCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldCycleStore(
    var items: MutableMap<String, HoldCycle>,
    var order: MutableList<String>,
) : HoldCycleStore {
    override fun find(code: String): HoldCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldCycle) {
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

    override fun list(): MutableList<HoldCycle> {
        val out = mutableListOf<HoldCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldCycleStore(): HoldCycleStore {
    return MemoryHoldCycleStore(mutableMapOf<String, HoldCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldCycleStore(
    var inner: HoldCycleStore,
    var cache: TtlCache<HoldCycle>,
    var hits: Int,
    var misses: Int,
) : HoldCycleStore {
    override fun find(code: String): HoldCycle? {
        val hit = (cache.get(code) as HoldCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldCycle> = inner.list()
}

fun newCachedHoldCycleStore(inner: HoldCycleStore, ttl: Int, clock: Clock): HoldCycleStore {
    return CachedHoldCycleStore(
        inner = inner,
        cache = newTtlCache<HoldCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold cycles. Empty fields match all. */
data class HoldCycleQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<HoldCycle>): MutableList<HoldCycle> {
        val matched = filter(items) { candidate: HoldCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldCycles(store: HoldCycleStore, query: HoldCycleQuery): MutableList<HoldCycle> {
    return query.apply(store.list())
}
