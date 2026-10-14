package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss slots by code. */
interface NearMissSlotStore {
    fun find(code: String): NearMissSlot?
    fun save(item: NearMissSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissSlotStore(
    var items: MutableMap<String, NearMissSlot>,
    var order: MutableList<String>,
) : NearMissSlotStore {
    override fun find(code: String): NearMissSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissSlot) {
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

    override fun list(): MutableList<NearMissSlot> {
        val out = mutableListOf<NearMissSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissSlotStore(): NearMissSlotStore {
    return MemoryNearMissSlotStore(mutableMapOf<String, NearMissSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissSlotStore(
    var inner: NearMissSlotStore,
    var cache: TtlCache<NearMissSlot>,
    var hits: Int,
    var misses: Int,
) : NearMissSlotStore {
    override fun find(code: String): NearMissSlot? {
        val hit = (cache.get(code) as NearMissSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissSlot> = inner.list()
}

fun newCachedNearMissSlotStore(inner: NearMissSlotStore, ttl: Int, clock: Clock): NearMissSlotStore {
    return CachedNearMissSlotStore(
        inner = inner,
        cache = newTtlCache<NearMissSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss slots. Empty fields match all. */
data class NearMissSlotQuery(
    var channel: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissSlot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissSlot>): MutableList<NearMissSlot> {
        val matched = filter(items) { candidate: NearMissSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissSlots(store: NearMissSlotStore, query: NearMissSlotQuery): MutableList<NearMissSlot> {
    return query.apply(store.list())
}
