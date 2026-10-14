package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel cycles by code. */
interface ParcelCycleStore {
    fun find(code: String): ParcelCycle?
    fun save(item: ParcelCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelCycleStore(
    var items: MutableMap<String, ParcelCycle>,
    var order: MutableList<String>,
) : ParcelCycleStore {
    override fun find(code: String): ParcelCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelCycle) {
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

    override fun list(): MutableList<ParcelCycle> {
        val out = mutableListOf<ParcelCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelCycleStore(): ParcelCycleStore {
    return MemoryParcelCycleStore(mutableMapOf<String, ParcelCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelCycleStore(
    var inner: ParcelCycleStore,
    var cache: TtlCache<ParcelCycle>,
    var hits: Int,
    var misses: Int,
) : ParcelCycleStore {
    override fun find(code: String): ParcelCycle? {
        val hit = (cache.get(code) as ParcelCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelCycle> = inner.list()
}

fun newCachedParcelCycleStore(inner: ParcelCycleStore, ttl: Int, clock: Clock): ParcelCycleStore {
    return CachedParcelCycleStore(
        inner = inner,
        cache = newTtlCache<ParcelCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel cycles. Empty fields match all. */
data class ParcelCycleQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelCycle>): MutableList<ParcelCycle> {
        val matched = filter(items) { candidate: ParcelCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelCycles(store: ParcelCycleStore, query: ParcelCycleQuery): MutableList<ParcelCycle> {
    return query.apply(store.list())
}
