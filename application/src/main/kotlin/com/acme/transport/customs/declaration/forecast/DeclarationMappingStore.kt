package com.acme.transport.customs.declaration.forecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration mappings by code. */
interface DeclarationMappingStore {
    fun find(code: String): DeclarationMapping?
    fun save(item: DeclarationMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationMappingStore(
    var items: MutableMap<String, DeclarationMapping>,
    var order: MutableList<String>,
) : DeclarationMappingStore {
    override fun find(code: String): DeclarationMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationMapping) {
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

    override fun list(): MutableList<DeclarationMapping> {
        val out = mutableListOf<DeclarationMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationMappingStore(): DeclarationMappingStore {
    return MemoryDeclarationMappingStore(mutableMapOf<String, DeclarationMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationMappingStore(
    var inner: DeclarationMappingStore,
    var cache: TtlCache<DeclarationMapping>,
    var hits: Int,
    var misses: Int,
) : DeclarationMappingStore {
    override fun find(code: String): DeclarationMapping? {
        val hit = (cache.get(code) as DeclarationMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationMapping> = inner.list()
}

fun newCachedDeclarationMappingStore(inner: DeclarationMappingStore, ttl: Int, clock: Clock): DeclarationMappingStore {
    return CachedDeclarationMappingStore(
        inner = inner,
        cache = newTtlCache<DeclarationMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration mappings. Empty fields match all. */
data class DeclarationMappingQuery(
    var lane: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationMapping>): MutableList<DeclarationMapping> {
        val matched = filter(items) { candidate: DeclarationMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationMappings(store: DeclarationMappingStore, query: DeclarationMappingQuery): MutableList<DeclarationMapping> {
    return query.apply(store.list())
}
