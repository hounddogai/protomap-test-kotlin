package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual camera event slots by code. */
interface ManualCameraEventSlotStore {
    fun find(code: String): ManualCameraEventSlot?
    fun save(item: ManualCameraEventSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCameraEventSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCameraEventSlotStore(
    var items: MutableMap<String, ManualCameraEventSlot>,
    var order: MutableList<String>,
) : ManualCameraEventSlotStore {
    override fun find(code: String): ManualCameraEventSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCameraEventSlot) {
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

    override fun list(): MutableList<ManualCameraEventSlot> {
        val out = mutableListOf<ManualCameraEventSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCameraEventSlotStore(): ManualCameraEventSlotStore {
    return MemoryManualCameraEventSlotStore(
        mutableMapOf<String, ManualCameraEventSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCameraEventSlotStore(
    var inner: ManualCameraEventSlotStore,
    var cache: TtlCache<ManualCameraEventSlot>,
    var hits: Int,
    var misses: Int,
) : ManualCameraEventSlotStore {
    override fun find(code: String): ManualCameraEventSlot? {
        val hit = (cache.get(code) as ManualCameraEventSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCameraEventSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCameraEventSlot> = inner.list()
}

fun newCachedManualCameraEventSlotStore(inner: ManualCameraEventSlotStore, ttl: Int, clock: Clock): ManualCameraEventSlotStore {
    return CachedManualCameraEventSlotStore(
        inner = inner,
        cache = newTtlCache<ManualCameraEventSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual camera event slots. Empty fields match all. */
data class ManualCameraEventSlotQuery(
    var source: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCameraEventSlot): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCameraEventSlot>): MutableList<ManualCameraEventSlot> {
        val matched = filter(items) { candidate: ManualCameraEventSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCameraEventSlots(store: ManualCameraEventSlotStore, query: ManualCameraEventSlotQuery): MutableList<ManualCameraEventSlot> {
    return query.apply(store.list())
}
