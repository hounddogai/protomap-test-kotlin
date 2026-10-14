package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient parcel slots by code. */
interface LenientParcelSlotStore {
    fun find(code: String): LenientParcelSlot?
    fun save(item: LenientParcelSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientParcelSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientParcelSlotStore(
    var items: MutableMap<String, LenientParcelSlot>,
    var order: MutableList<String>,
) : LenientParcelSlotStore {
    override fun find(code: String): LenientParcelSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientParcelSlot) {
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

    override fun list(): MutableList<LenientParcelSlot> {
        val out = mutableListOf<LenientParcelSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientParcelSlotStore(): LenientParcelSlotStore {
    return MemoryLenientParcelSlotStore(mutableMapOf<String, LenientParcelSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientParcelSlotStore(
    var inner: LenientParcelSlotStore,
    var cache: TtlCache<LenientParcelSlot>,
    var hits: Int,
    var misses: Int,
) : LenientParcelSlotStore {
    override fun find(code: String): LenientParcelSlot? {
        val hit = (cache.get(code) as LenientParcelSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientParcelSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientParcelSlot> = inner.list()
}

fun newCachedLenientParcelSlotStore(inner: LenientParcelSlotStore, ttl: Int, clock: Clock): LenientParcelSlotStore {
    return CachedLenientParcelSlotStore(
        inner = inner,
        cache = newTtlCache<LenientParcelSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient parcel slots. Empty fields match all. */
data class LenientParcelSlotQuery(
    var source: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientParcelSlot): Boolean {
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

    fun apply(items: MutableList<LenientParcelSlot>): MutableList<LenientParcelSlot> {
        val matched = filter(items) { candidate: LenientParcelSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientParcelSlots(store: LenientParcelSlotStore, query: LenientParcelSlotQuery): MutableList<LenientParcelSlot> {
    return query.apply(store.list())
}
