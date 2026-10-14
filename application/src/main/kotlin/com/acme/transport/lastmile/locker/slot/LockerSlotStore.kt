package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker slots by code. */
interface LockerSlotStore {
    fun find(code: String): LockerSlot?
    fun save(item: LockerSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerSlotStore(
    var items: MutableMap<String, LockerSlot>,
    var order: MutableList<String>,
) : LockerSlotStore {
    override fun find(code: String): LockerSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerSlot) {
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

    override fun list(): MutableList<LockerSlot> {
        val out = mutableListOf<LockerSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerSlotStore(): LockerSlotStore {
    return MemoryLockerSlotStore(mutableMapOf<String, LockerSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerSlotStore(
    var inner: LockerSlotStore,
    var cache: TtlCache<LockerSlot>,
    var hits: Int,
    var misses: Int,
) : LockerSlotStore {
    override fun find(code: String): LockerSlot? {
        val hit = (cache.get(code) as LockerSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerSlot> = inner.list()
}

fun newCachedLockerSlotStore(inner: LockerSlotStore, ttl: Int, clock: Clock): LockerSlotStore {
    return CachedLockerSlotStore(
        inner = inner,
        cache = newTtlCache<LockerSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker slots. Empty fields match all. */
data class LockerSlotQuery(
    var locale: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerSlot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerSlot>): MutableList<LockerSlot> {
        val matched = filter(items) { candidate: LockerSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerSlots(store: LockerSlotStore, query: LockerSlotQuery): MutableList<LockerSlot> {
    return query.apply(store.list())
}
