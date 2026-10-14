package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default ev charge slots by code. */
interface DefaultEvChargeSlotStore {
    fun find(code: String): DefaultEvChargeSlot?
    fun save(item: DefaultEvChargeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultEvChargeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultEvChargeSlotStore(
    var items: MutableMap<String, DefaultEvChargeSlot>,
    var order: MutableList<String>,
) : DefaultEvChargeSlotStore {
    override fun find(code: String): DefaultEvChargeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultEvChargeSlot) {
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

    override fun list(): MutableList<DefaultEvChargeSlot> {
        val out = mutableListOf<DefaultEvChargeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultEvChargeSlotStore(): DefaultEvChargeSlotStore {
    return MemoryDefaultEvChargeSlotStore(mutableMapOf<String, DefaultEvChargeSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultEvChargeSlotStore(
    var inner: DefaultEvChargeSlotStore,
    var cache: TtlCache<DefaultEvChargeSlot>,
    var hits: Int,
    var misses: Int,
) : DefaultEvChargeSlotStore {
    override fun find(code: String): DefaultEvChargeSlot? {
        val hit = (cache.get(code) as DefaultEvChargeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultEvChargeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultEvChargeSlot> = inner.list()
}

fun newCachedDefaultEvChargeSlotStore(inner: DefaultEvChargeSlotStore, ttl: Int, clock: Clock): DefaultEvChargeSlotStore {
    return CachedDefaultEvChargeSlotStore(
        inner = inner,
        cache = newTtlCache<DefaultEvChargeSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default ev charge slots. Empty fields match all. */
data class DefaultEvChargeSlotQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultEvChargeSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultEvChargeSlot>): MutableList<DefaultEvChargeSlot> {
        val matched = filter(items) { candidate: DefaultEvChargeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultEvChargeSlots(store: DefaultEvChargeSlotStore, query: DefaultEvChargeSlotQuery): MutableList<DefaultEvChargeSlot> {
    return query.apply(store.list())
}
