package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell slots by code. */
interface DwellSlotStore {
    fun find(code: String): DwellSlot?
    fun save(item: DwellSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellSlotStore(
    var items: MutableMap<String, DwellSlot>,
    var order: MutableList<String>,
) : DwellSlotStore {
    override fun find(code: String): DwellSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellSlot) {
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

    override fun list(): MutableList<DwellSlot> {
        val out = mutableListOf<DwellSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellSlotStore(): DwellSlotStore {
    return MemoryDwellSlotStore(mutableMapOf<String, DwellSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellSlotStore(
    var inner: DwellSlotStore,
    var cache: TtlCache<DwellSlot>,
    var hits: Int,
    var misses: Int,
) : DwellSlotStore {
    override fun find(code: String): DwellSlot? {
        val hit = (cache.get(code) as DwellSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellSlot> = inner.list()
}

fun newCachedDwellSlotStore(inner: DwellSlotStore, ttl: Int, clock: Clock): DwellSlotStore {
    return CachedDwellSlotStore(
        inner = inner,
        cache = newTtlCache<DwellSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell slots. Empty fields match all. */
data class DwellSlotQuery(
    var source: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellSlot): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellSlot>): MutableList<DwellSlot> {
        val matched = filter(items) { candidate: DwellSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellSlots(store: DwellSlotStore, query: DwellSlotQuery): MutableList<DwellSlot> {
    return query.apply(store.list())
}
