package com.acme.transport.customs.hold.mapping

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold slots by code. */
interface HoldSlotStore {
    fun find(code: String): HoldSlot?
    fun save(item: HoldSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldSlotStore(
    var items: MutableMap<String, HoldSlot>,
    var order: MutableList<String>,
) : HoldSlotStore {
    override fun find(code: String): HoldSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldSlot) {
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

    override fun list(): MutableList<HoldSlot> {
        val out = mutableListOf<HoldSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldSlotStore(): HoldSlotStore {
    return MemoryHoldSlotStore(mutableMapOf<String, HoldSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldSlotStore(
    var inner: HoldSlotStore,
    var cache: TtlCache<HoldSlot>,
    var hits: Int,
    var misses: Int,
) : HoldSlotStore {
    override fun find(code: String): HoldSlot? {
        val hit = (cache.get(code) as HoldSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldSlot> = inner.list()
}

fun newCachedHoldSlotStore(inner: HoldSlotStore, ttl: Int, clock: Clock): HoldSlotStore {
    return CachedHoldSlotStore(
        inner = inner,
        cache = newTtlCache<HoldSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold slots. Empty fields match all. */
data class HoldSlotQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldSlot>): MutableList<HoldSlot> {
        val matched = filter(items) { candidate: HoldSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldSlots(store: HoldSlotStore, query: HoldSlotQuery): MutableList<HoldSlot> = query.apply(store.list())
