package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge groups by code. */
interface EvChargeGroupStore {
    fun find(code: String): EvChargeGroup?
    fun save(item: EvChargeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeGroupStore(
    var items: MutableMap<String, EvChargeGroup>,
    var order: MutableList<String>,
) : EvChargeGroupStore {
    override fun find(code: String): EvChargeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeGroup) {
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

    override fun list(): MutableList<EvChargeGroup> {
        val out = mutableListOf<EvChargeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeGroupStore(): EvChargeGroupStore {
    return MemoryEvChargeGroupStore(mutableMapOf<String, EvChargeGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeGroupStore(
    var inner: EvChargeGroupStore,
    var cache: TtlCache<EvChargeGroup>,
    var hits: Int,
    var misses: Int,
) : EvChargeGroupStore {
    override fun find(code: String): EvChargeGroup? {
        val hit = (cache.get(code) as EvChargeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeGroup> = inner.list()
}

fun newCachedEvChargeGroupStore(inner: EvChargeGroupStore, ttl: Int, clock: Clock): EvChargeGroupStore {
    return CachedEvChargeGroupStore(
        inner = inner,
        cache = newTtlCache<EvChargeGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge groups. Empty fields match all. */
data class EvChargeGroupQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeGroup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeGroup>): MutableList<EvChargeGroup> {
        val matched = filter(items) { candidate: EvChargeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeGroups(store: EvChargeGroupStore, query: EvChargeGroupQuery): MutableList<EvChargeGroup> {
    return query.apply(store.list())
}
