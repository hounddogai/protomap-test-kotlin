package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default locker slots by code. */
interface DefaultLockerSlotStore {
    fun find(code: String): DefaultLockerSlot?
    fun save(item: DefaultLockerSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultLockerSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultLockerSlotStore(
    var items: MutableMap<String, DefaultLockerSlot>,
    var order: MutableList<String>,
) : DefaultLockerSlotStore {
    override fun find(code: String): DefaultLockerSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultLockerSlot) {
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

    override fun list(): MutableList<DefaultLockerSlot> {
        val out = mutableListOf<DefaultLockerSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultLockerSlotStore(): DefaultLockerSlotStore {
    return MemoryDefaultLockerSlotStore(mutableMapOf<String, DefaultLockerSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultLockerSlotStore(
    var inner: DefaultLockerSlotStore,
    var cache: TtlCache<DefaultLockerSlot>,
    var hits: Int,
    var misses: Int,
) : DefaultLockerSlotStore {
    override fun find(code: String): DefaultLockerSlot? {
        val hit = (cache.get(code) as DefaultLockerSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultLockerSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultLockerSlot> = inner.list()
}

fun newCachedDefaultLockerSlotStore(inner: DefaultLockerSlotStore, ttl: Int, clock: Clock): DefaultLockerSlotStore {
    return CachedDefaultLockerSlotStore(
        inner = inner,
        cache = newTtlCache<DefaultLockerSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default locker slots. Empty fields match all. */
data class DefaultLockerSlotQuery(
    var segment: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultLockerSlot): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultLockerSlot>): MutableList<DefaultLockerSlot> {
        val matched = filter(items) { candidate: DefaultLockerSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultLockerSlots(store: DefaultLockerSlotStore, query: DefaultLockerSlotQuery): MutableList<DefaultLockerSlot> {
    return query.apply(store.list())
}
