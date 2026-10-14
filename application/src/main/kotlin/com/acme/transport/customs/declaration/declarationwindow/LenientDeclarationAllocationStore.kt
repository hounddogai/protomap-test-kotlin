package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient declaration allocations by code. */
interface LenientDeclarationAllocationStore {
    fun find(code: String): LenientDeclarationAllocation?
    fun save(item: LenientDeclarationAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDeclarationAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDeclarationAllocationStore(
    var items: MutableMap<String, LenientDeclarationAllocation>,
    var order: MutableList<String>,
) : LenientDeclarationAllocationStore {
    override fun find(code: String): LenientDeclarationAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDeclarationAllocation) {
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

    override fun list(): MutableList<LenientDeclarationAllocation> {
        val out = mutableListOf<LenientDeclarationAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDeclarationAllocationStore(): LenientDeclarationAllocationStore {
    return MemoryLenientDeclarationAllocationStore(
        mutableMapOf<String, LenientDeclarationAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDeclarationAllocationStore(
    var inner: LenientDeclarationAllocationStore,
    var cache: TtlCache<LenientDeclarationAllocation>,
    var hits: Int,
    var misses: Int,
) : LenientDeclarationAllocationStore {
    override fun find(code: String): LenientDeclarationAllocation? {
        val hit = (cache.get(code) as LenientDeclarationAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDeclarationAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDeclarationAllocation> = inner.list()
}

fun newCachedLenientDeclarationAllocationStore(inner: LenientDeclarationAllocationStore, ttl: Int, clock: Clock): LenientDeclarationAllocationStore {
    return CachedLenientDeclarationAllocationStore(
        inner = inner,
        cache = newTtlCache<LenientDeclarationAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient declaration allocations. Empty fields match all. */
data class LenientDeclarationAllocationQuery(
    var category: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDeclarationAllocation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<LenientDeclarationAllocation>): MutableList<LenientDeclarationAllocation> {
        val matched = filter(items) { candidate: LenientDeclarationAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDeclarationAllocations(store: LenientDeclarationAllocationStore, query: LenientDeclarationAllocationQuery): MutableList<LenientDeclarationAllocation> {
    return query.apply(store.list())
}
