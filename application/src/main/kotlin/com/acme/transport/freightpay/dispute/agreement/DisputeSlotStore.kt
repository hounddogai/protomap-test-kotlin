package com.acme.transport.freightpay.dispute.agreement

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute slots by code. */
interface DisputeSlotStore {
    fun find(code: String): DisputeSlot?
    fun save(item: DisputeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeSlotStore(
    var items: MutableMap<String, DisputeSlot>,
    var order: MutableList<String>,
) : DisputeSlotStore {
    override fun find(code: String): DisputeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeSlot) {
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

    override fun list(): MutableList<DisputeSlot> {
        val out = mutableListOf<DisputeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeSlotStore(): DisputeSlotStore {
    return MemoryDisputeSlotStore(mutableMapOf<String, DisputeSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeSlotStore(
    var inner: DisputeSlotStore,
    var cache: TtlCache<DisputeSlot>,
    var hits: Int,
    var misses: Int,
) : DisputeSlotStore {
    override fun find(code: String): DisputeSlot? {
        val hit = (cache.get(code) as DisputeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeSlot> = inner.list()
}

fun newCachedDisputeSlotStore(inner: DisputeSlotStore, ttl: Int, clock: Clock): DisputeSlotStore {
    return CachedDisputeSlotStore(
        inner = inner,
        cache = newTtlCache<DisputeSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute slots. Empty fields match all. */
data class DisputeSlotQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeSlot>): MutableList<DisputeSlot> {
        val matched = filter(items) { candidate: DisputeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeSlots(store: DisputeSlotStore, query: DisputeSlotQuery): MutableList<DisputeSlot> {
    return query.apply(store.list())
}
