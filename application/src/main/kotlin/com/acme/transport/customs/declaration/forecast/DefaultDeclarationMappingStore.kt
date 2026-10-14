package com.acme.transport.customs.declaration.forecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default declaration mappings by code. */
interface DefaultDeclarationMappingStore {
    fun find(code: String): DefaultDeclarationMapping?
    fun save(item: DefaultDeclarationMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDeclarationMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDeclarationMappingStore(
    var items: MutableMap<String, DefaultDeclarationMapping>,
    var order: MutableList<String>,
) : DefaultDeclarationMappingStore {
    override fun find(code: String): DefaultDeclarationMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDeclarationMapping) {
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

    override fun list(): MutableList<DefaultDeclarationMapping> {
        val out = mutableListOf<DefaultDeclarationMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDeclarationMappingStore(): DefaultDeclarationMappingStore {
    return MemoryDefaultDeclarationMappingStore(
        mutableMapOf<String, DefaultDeclarationMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDeclarationMappingStore(
    var inner: DefaultDeclarationMappingStore,
    var cache: TtlCache<DefaultDeclarationMapping>,
    var hits: Int,
    var misses: Int,
) : DefaultDeclarationMappingStore {
    override fun find(code: String): DefaultDeclarationMapping? {
        val hit = (cache.get(code) as DefaultDeclarationMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDeclarationMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDeclarationMapping> = inner.list()
}

fun newCachedDefaultDeclarationMappingStore(inner: DefaultDeclarationMappingStore, ttl: Int, clock: Clock): DefaultDeclarationMappingStore {
    return CachedDefaultDeclarationMappingStore(
        inner = inner,
        cache = newTtlCache<DefaultDeclarationMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default declaration mappings. Empty fields match all. */
data class DefaultDeclarationMappingQuery(
    var lane: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDeclarationMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<DefaultDeclarationMapping>): MutableList<DefaultDeclarationMapping> {
        val matched = filter(items) { candidate: DefaultDeclarationMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDeclarationMappings(store: DefaultDeclarationMappingStore, query: DefaultDeclarationMappingQuery): MutableList<DefaultDeclarationMapping> {
    return query.apply(store.list())
}
