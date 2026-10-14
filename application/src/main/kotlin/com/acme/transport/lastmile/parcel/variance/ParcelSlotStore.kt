package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel slots by code. */
interface ParcelSlotStore {
    fun find(code: String): ParcelSlot?
    fun save(item: ParcelSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelSlotStore(
    var items: MutableMap<String, ParcelSlot>,
    var order: MutableList<String>,
) : ParcelSlotStore {
    override fun find(code: String): ParcelSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelSlot) {
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

    override fun list(): MutableList<ParcelSlot> {
        val out = mutableListOf<ParcelSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelSlotStore(): ParcelSlotStore {
    return MemoryParcelSlotStore(mutableMapOf<String, ParcelSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelSlotStore(
    var inner: ParcelSlotStore,
    var cache: TtlCache<ParcelSlot>,
    var hits: Int,
    var misses: Int,
) : ParcelSlotStore {
    override fun find(code: String): ParcelSlot? {
        val hit = (cache.get(code) as ParcelSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelSlot> = inner.list()
}

fun newCachedParcelSlotStore(inner: ParcelSlotStore, ttl: Int, clock: Clock): ParcelSlotStore {
    return CachedParcelSlotStore(
        inner = inner,
        cache = newTtlCache<ParcelSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel slots. Empty fields match all. */
data class ParcelSlotQuery(
    var locale: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelSlot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<ParcelSlot>): MutableList<ParcelSlot> {
        val matched = filter(items) { candidate: ParcelSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelSlots(store: ParcelSlotStore, query: ParcelSlotQuery): MutableList<ParcelSlot> {
    return query.apply(store.list())
}
