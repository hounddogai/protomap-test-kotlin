package com.acme.transport.network.cutoff.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff slots by code. */
interface CutoffSlotStore {
    fun find(code: String): CutoffSlot?
    fun save(item: CutoffSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffSlotStore(
    var items: MutableMap<String, CutoffSlot>,
    var order: MutableList<String>,
) : CutoffSlotStore {
    override fun find(code: String): CutoffSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffSlot) {
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

    override fun list(): MutableList<CutoffSlot> {
        val out = mutableListOf<CutoffSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffSlotStore(): CutoffSlotStore {
    return MemoryCutoffSlotStore(mutableMapOf<String, CutoffSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffSlotStore(
    var inner: CutoffSlotStore,
    var cache: TtlCache<CutoffSlot>,
    var hits: Int,
    var misses: Int,
) : CutoffSlotStore {
    override fun find(code: String): CutoffSlot? {
        val hit = (cache.get(code) as CutoffSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffSlot> = inner.list()
}

fun newCachedCutoffSlotStore(inner: CutoffSlotStore, ttl: Int, clock: Clock): CutoffSlotStore {
    return CachedCutoffSlotStore(
        inner = inner,
        cache = newTtlCache<CutoffSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff slots. Empty fields match all. */
data class CutoffSlotQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<CutoffSlot>): MutableList<CutoffSlot> {
        val matched = filter(items) { candidate: CutoffSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffSlots(store: CutoffSlotStore, query: CutoffSlotQuery): MutableList<CutoffSlot> {
    return query.apply(store.list())
}
