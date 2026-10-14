package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation slots by code. */
interface SortationSlotStore {
    fun find(code: String): SortationSlot?
    fun save(item: SortationSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationSlotStore(
    var items: MutableMap<String, SortationSlot>,
    var order: MutableList<String>,
) : SortationSlotStore {
    override fun find(code: String): SortationSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationSlot) {
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

    override fun list(): MutableList<SortationSlot> {
        val out = mutableListOf<SortationSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationSlotStore(): SortationSlotStore {
    return MemorySortationSlotStore(mutableMapOf<String, SortationSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationSlotStore(
    var inner: SortationSlotStore,
    var cache: TtlCache<SortationSlot>,
    var hits: Int,
    var misses: Int,
) : SortationSlotStore {
    override fun find(code: String): SortationSlot? {
        val hit = (cache.get(code) as SortationSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationSlot> = inner.list()
}

fun newCachedSortationSlotStore(inner: SortationSlotStore, ttl: Int, clock: Clock): SortationSlotStore {
    return CachedSortationSlotStore(
        inner = inner,
        cache = newTtlCache<SortationSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation slots. Empty fields match all. */
data class SortationSlotQuery(
    var locale: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationSlot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationSlot>): MutableList<SortationSlot> {
        val matched = filter(items) { candidate: SortationSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationSlots(store: SortationSlotStore, query: SortationSlotQuery): MutableList<SortationSlot> {
    return query.apply(store.list())
}
