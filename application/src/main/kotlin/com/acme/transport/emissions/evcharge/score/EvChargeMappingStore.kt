package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge mappings by code. */
interface EvChargeMappingStore {
    fun find(code: String): EvChargeMapping?
    fun save(item: EvChargeMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeMappingStore(
    var items: MutableMap<String, EvChargeMapping>,
    var order: MutableList<String>,
) : EvChargeMappingStore {
    override fun find(code: String): EvChargeMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeMapping) {
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

    override fun list(): MutableList<EvChargeMapping> {
        val out = mutableListOf<EvChargeMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeMappingStore(): EvChargeMappingStore {
    return MemoryEvChargeMappingStore(mutableMapOf<String, EvChargeMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeMappingStore(
    var inner: EvChargeMappingStore,
    var cache: TtlCache<EvChargeMapping>,
    var hits: Int,
    var misses: Int,
) : EvChargeMappingStore {
    override fun find(code: String): EvChargeMapping? {
        val hit = (cache.get(code) as EvChargeMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeMapping> = inner.list()
}

fun newCachedEvChargeMappingStore(inner: EvChargeMappingStore, ttl: Int, clock: Clock): EvChargeMappingStore {
    return CachedEvChargeMappingStore(
        inner = inner,
        cache = newTtlCache<EvChargeMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge mappings. Empty fields match all. */
data class EvChargeMappingQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<EvChargeMapping>): MutableList<EvChargeMapping> {
        val matched = filter(items) { candidate: EvChargeMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeMappings(store: EvChargeMappingStore, query: EvChargeMappingQuery): MutableList<EvChargeMapping> {
    return query.apply(store.list())
}
