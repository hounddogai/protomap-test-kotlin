package com.acme.transport.terminal.dwell.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling dwell slots by code. */
interface RollingDwellSlotStore {
    fun find(code: String): RollingDwellSlot?
    fun save(item: RollingDwellSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDwellSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDwellSlotStore(
    var items: MutableMap<String, RollingDwellSlot>,
    var order: MutableList<String>,
) : RollingDwellSlotStore {
    override fun find(code: String): RollingDwellSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDwellSlot) {
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

    override fun list(): MutableList<RollingDwellSlot> {
        val out = mutableListOf<RollingDwellSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDwellSlotStore(): RollingDwellSlotStore {
    return MemoryRollingDwellSlotStore(mutableMapOf<String, RollingDwellSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDwellSlotStore(
    var inner: RollingDwellSlotStore,
    var cache: TtlCache<RollingDwellSlot>,
    var hits: Int,
    var misses: Int,
) : RollingDwellSlotStore {
    override fun find(code: String): RollingDwellSlot? {
        val hit = (cache.get(code) as RollingDwellSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDwellSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDwellSlot> = inner.list()
}

fun newCachedRollingDwellSlotStore(inner: RollingDwellSlotStore, ttl: Int, clock: Clock): RollingDwellSlotStore {
    return CachedRollingDwellSlotStore(
        inner = inner,
        cache = newTtlCache<RollingDwellSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dwell slots. Empty fields match all. */
data class RollingDwellSlotQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDwellSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDwellSlot>): MutableList<RollingDwellSlot> {
        val matched = filter(items) { candidate: RollingDwellSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDwellSlots(store: RollingDwellSlotStore, query: RollingDwellSlotQuery): MutableList<RollingDwellSlot> {
    return query.apply(store.list())
}
