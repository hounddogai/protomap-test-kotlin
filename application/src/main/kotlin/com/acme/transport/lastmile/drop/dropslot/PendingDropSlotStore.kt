package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending drop slots by code. */
interface PendingDropSlotStore {
    fun find(code: String): PendingDropSlot?
    fun save(item: PendingDropSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDropSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDropSlotStore(
    var items: MutableMap<String, PendingDropSlot>,
    var order: MutableList<String>,
) : PendingDropSlotStore {
    override fun find(code: String): PendingDropSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDropSlot) {
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

    override fun list(): MutableList<PendingDropSlot> {
        val out = mutableListOf<PendingDropSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDropSlotStore(): PendingDropSlotStore {
    return MemoryPendingDropSlotStore(mutableMapOf<String, PendingDropSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDropSlotStore(
    var inner: PendingDropSlotStore,
    var cache: TtlCache<PendingDropSlot>,
    var hits: Int,
    var misses: Int,
) : PendingDropSlotStore {
    override fun find(code: String): PendingDropSlot? {
        val hit = (cache.get(code) as PendingDropSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDropSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDropSlot> = inner.list()
}

fun newCachedPendingDropSlotStore(inner: PendingDropSlotStore, ttl: Int, clock: Clock): PendingDropSlotStore {
    return CachedPendingDropSlotStore(
        inner = inner,
        cache = newTtlCache<PendingDropSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending drop slots. Empty fields match all. */
data class PendingDropSlotQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDropSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDropSlot>): MutableList<PendingDropSlot> {
        val matched = filter(items) { candidate: PendingDropSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDropSlots(store: PendingDropSlotStore, query: PendingDropSlotQuery): MutableList<PendingDropSlot> {
    return query.apply(store.list())
}
