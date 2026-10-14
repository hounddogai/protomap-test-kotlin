package com.acme.transport.customs.tariff.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff slots by code. */
interface TariffSlotStore {
    fun find(code: String): TariffSlot?
    fun save(item: TariffSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffSlotStore(
    var items: MutableMap<String, TariffSlot>,
    var order: MutableList<String>,
) : TariffSlotStore {
    override fun find(code: String): TariffSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffSlot) {
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

    override fun list(): MutableList<TariffSlot> {
        val out = mutableListOf<TariffSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffSlotStore(): TariffSlotStore {
    return MemoryTariffSlotStore(mutableMapOf<String, TariffSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffSlotStore(
    var inner: TariffSlotStore,
    var cache: TtlCache<TariffSlot>,
    var hits: Int,
    var misses: Int,
) : TariffSlotStore {
    override fun find(code: String): TariffSlot? {
        val hit = (cache.get(code) as TariffSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffSlot> = inner.list()
}

fun newCachedTariffSlotStore(inner: TariffSlotStore, ttl: Int, clock: Clock): TariffSlotStore {
    return CachedTariffSlotStore(
        inner = inner,
        cache = newTtlCache<TariffSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff slots. Empty fields match all. */
data class TariffSlotQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffSlot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffSlot>): MutableList<TariffSlot> {
        val matched = filter(items) { candidate: TariffSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffSlots(store: TariffSlotStore, query: TariffSlotQuery): MutableList<TariffSlot> {
    return query.apply(store.list())
}
