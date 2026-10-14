package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker cycles by code. */
interface LockerCycleStore {
    fun find(code: String): LockerCycle?
    fun save(item: LockerCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerCycleStore(
    var items: MutableMap<String, LockerCycle>,
    var order: MutableList<String>,
) : LockerCycleStore {
    override fun find(code: String): LockerCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerCycle) {
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

    override fun list(): MutableList<LockerCycle> {
        val out = mutableListOf<LockerCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerCycleStore(): LockerCycleStore {
    return MemoryLockerCycleStore(mutableMapOf<String, LockerCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerCycleStore(
    var inner: LockerCycleStore,
    var cache: TtlCache<LockerCycle>,
    var hits: Int,
    var misses: Int,
) : LockerCycleStore {
    override fun find(code: String): LockerCycle? {
        val hit = (cache.get(code) as LockerCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerCycle> = inner.list()
}

fun newCachedLockerCycleStore(inner: LockerCycleStore, ttl: Int, clock: Clock): LockerCycleStore {
    return CachedLockerCycleStore(
        inner = inner,
        cache = newTtlCache<LockerCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker cycles. Empty fields match all. */
data class LockerCycleQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerCycle): Boolean {
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

    fun apply(items: MutableList<LockerCycle>): MutableList<LockerCycle> {
        val matched = filter(items) { candidate: LockerCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerCycles(store: LockerCycleStore, query: LockerCycleQuery): MutableList<LockerCycle> {
    return query.apply(store.list())
}
