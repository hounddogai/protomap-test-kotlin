package com.acme.transport.customs.declaration.plan

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration allocations by code. */
interface DeclarationAllocationStore {
    fun find(code: String): DeclarationAllocation?
    fun save(item: DeclarationAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationAllocationStore(
    var items: MutableMap<String, DeclarationAllocation>,
    var order: MutableList<String>,
) : DeclarationAllocationStore {
    override fun find(code: String): DeclarationAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationAllocation) {
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

    override fun list(): MutableList<DeclarationAllocation> {
        val out = mutableListOf<DeclarationAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationAllocationStore(): DeclarationAllocationStore {
    return MemoryDeclarationAllocationStore(
        mutableMapOf<String, DeclarationAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationAllocationStore(
    var inner: DeclarationAllocationStore,
    var cache: TtlCache<DeclarationAllocation>,
    var hits: Int,
    var misses: Int,
) : DeclarationAllocationStore {
    override fun find(code: String): DeclarationAllocation? {
        val hit = (cache.get(code) as DeclarationAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationAllocation> = inner.list()
}

fun newCachedDeclarationAllocationStore(inner: DeclarationAllocationStore, ttl: Int, clock: Clock): DeclarationAllocationStore {
    return CachedDeclarationAllocationStore(
        inner = inner,
        cache = newTtlCache<DeclarationAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration allocations. Empty fields match all. */
data class DeclarationAllocationQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationAllocation>): MutableList<DeclarationAllocation> {
        val matched = filter(items) { candidate: DeclarationAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationAllocations(store: DeclarationAllocationStore, query: DeclarationAllocationQuery): MutableList<DeclarationAllocation> {
    return query.apply(store.list())
}
