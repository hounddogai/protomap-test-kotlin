package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global locker slots by code. */
interface GlobalLockerSlotStore {
    fun find(code: String): GlobalLockerSlot?
    fun save(item: GlobalLockerSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalLockerSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalLockerSlotStore(
    var items: MutableMap<String, GlobalLockerSlot>,
    var order: MutableList<String>,
) : GlobalLockerSlotStore {
    override fun find(code: String): GlobalLockerSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalLockerSlot) {
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

    override fun list(): MutableList<GlobalLockerSlot> {
        val out = mutableListOf<GlobalLockerSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalLockerSlotStore(): GlobalLockerSlotStore {
    return MemoryGlobalLockerSlotStore(mutableMapOf<String, GlobalLockerSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalLockerSlotStore(
    var inner: GlobalLockerSlotStore,
    var cache: TtlCache<GlobalLockerSlot>,
    var hits: Int,
    var misses: Int,
) : GlobalLockerSlotStore {
    override fun find(code: String): GlobalLockerSlot? {
        val hit = (cache.get(code) as GlobalLockerSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalLockerSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalLockerSlot> = inner.list()
}

fun newCachedGlobalLockerSlotStore(inner: GlobalLockerSlotStore, ttl: Int, clock: Clock): GlobalLockerSlotStore {
    return CachedGlobalLockerSlotStore(
        inner = inner,
        cache = newTtlCache<GlobalLockerSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global locker slots. Empty fields match all. */
data class GlobalLockerSlotQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalLockerSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalLockerSlot>): MutableList<GlobalLockerSlot> {
        val matched = filter(items) { candidate: GlobalLockerSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalLockerSlots(store: GlobalLockerSlotStore, query: GlobalLockerSlotQuery): MutableList<GlobalLockerSlot> {
    return query.apply(store.list())
}
