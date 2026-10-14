package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul slots by code. */
interface BackhaulSlotStore {
    fun find(code: String): BackhaulSlot?
    fun save(item: BackhaulSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulSlotStore(
    var items: MutableMap<String, BackhaulSlot>,
    var order: MutableList<String>,
) : BackhaulSlotStore {
    override fun find(code: String): BackhaulSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulSlot) {
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

    override fun list(): MutableList<BackhaulSlot> {
        val out = mutableListOf<BackhaulSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulSlotStore(): BackhaulSlotStore {
    return MemoryBackhaulSlotStore(mutableMapOf<String, BackhaulSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulSlotStore(
    var inner: BackhaulSlotStore,
    var cache: TtlCache<BackhaulSlot>,
    var hits: Int,
    var misses: Int,
) : BackhaulSlotStore {
    override fun find(code: String): BackhaulSlot? {
        val hit = (cache.get(code) as BackhaulSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulSlot> = inner.list()
}

fun newCachedBackhaulSlotStore(inner: BackhaulSlotStore, ttl: Int, clock: Clock): BackhaulSlotStore {
    return CachedBackhaulSlotStore(
        inner = inner,
        cache = newTtlCache<BackhaulSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul slots. Empty fields match all. */
data class BackhaulSlotQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulSlot>): MutableList<BackhaulSlot> {
        val matched = filter(items) { candidate: BackhaulSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulSlots(store: BackhaulSlotStore, query: BackhaulSlotQuery): MutableList<BackhaulSlot> {
    return query.apply(store.list())
}
