package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict camera event cycles by code. */
interface StrictCameraEventCycleStore {
    fun find(code: String): StrictCameraEventCycle?
    fun save(item: StrictCameraEventCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCameraEventCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCameraEventCycleStore(
    var items: MutableMap<String, StrictCameraEventCycle>,
    var order: MutableList<String>,
) : StrictCameraEventCycleStore {
    override fun find(code: String): StrictCameraEventCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCameraEventCycle) {
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

    override fun list(): MutableList<StrictCameraEventCycle> {
        val out = mutableListOf<StrictCameraEventCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCameraEventCycleStore(): StrictCameraEventCycleStore {
    return MemoryStrictCameraEventCycleStore(
        mutableMapOf<String, StrictCameraEventCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCameraEventCycleStore(
    var inner: StrictCameraEventCycleStore,
    var cache: TtlCache<StrictCameraEventCycle>,
    var hits: Int,
    var misses: Int,
) : StrictCameraEventCycleStore {
    override fun find(code: String): StrictCameraEventCycle? {
        val hit = (cache.get(code) as StrictCameraEventCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCameraEventCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCameraEventCycle> = inner.list()
}

fun newCachedStrictCameraEventCycleStore(inner: StrictCameraEventCycleStore, ttl: Int, clock: Clock): StrictCameraEventCycleStore {
    return CachedStrictCameraEventCycleStore(
        inner = inner,
        cache = newTtlCache<StrictCameraEventCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict camera event cycles. Empty fields match all. */
data class StrictCameraEventCycleQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCameraEventCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<StrictCameraEventCycle>): MutableList<StrictCameraEventCycle> {
        val matched = filter(items) { candidate: StrictCameraEventCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCameraEventCycles(store: StrictCameraEventCycleStore, query: StrictCameraEventCycleQuery): MutableList<StrictCameraEventCycle> {
    return query.apply(store.list())
}
