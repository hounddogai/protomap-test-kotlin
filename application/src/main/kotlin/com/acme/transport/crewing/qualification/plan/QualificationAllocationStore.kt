package com.acme.transport.crewing.qualification.plan

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification allocations by code. */
interface QualificationAllocationStore {
    fun find(code: String): QualificationAllocation?
    fun save(item: QualificationAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationAllocationStore(
    var items: MutableMap<String, QualificationAllocation>,
    var order: MutableList<String>,
) : QualificationAllocationStore {
    override fun find(code: String): QualificationAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationAllocation) {
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

    override fun list(): MutableList<QualificationAllocation> {
        val out = mutableListOf<QualificationAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationAllocationStore(): QualificationAllocationStore {
    return MemoryQualificationAllocationStore(
        mutableMapOf<String, QualificationAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationAllocationStore(
    var inner: QualificationAllocationStore,
    var cache: TtlCache<QualificationAllocation>,
    var hits: Int,
    var misses: Int,
) : QualificationAllocationStore {
    override fun find(code: String): QualificationAllocation? {
        val hit = (cache.get(code) as QualificationAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationAllocation> = inner.list()
}

fun newCachedQualificationAllocationStore(inner: QualificationAllocationStore, ttl: Int, clock: Clock): QualificationAllocationStore {
    return CachedQualificationAllocationStore(
        inner = inner,
        cache = newTtlCache<QualificationAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification allocations. Empty fields match all. */
data class QualificationAllocationQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<QualificationAllocation>): MutableList<QualificationAllocation> {
        val matched = filter(items) { candidate: QualificationAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationAllocations(store: QualificationAllocationStore, query: QualificationAllocationQuery): MutableList<QualificationAllocation> {
    return query.apply(store.list())
}
