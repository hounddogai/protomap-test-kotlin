package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity exceptions by code. */
interface CapacityExceptionStore {
    fun find(code: String): CapacityException?
    fun save(item: CapacityException)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityExceptionStore(
    var items: MutableMap<String, CapacityException>,
    var order: MutableList<String>,
) : CapacityExceptionStore {
    override fun find(code: String): CapacityException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityException) {
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

    override fun list(): MutableList<CapacityException> {
        val out = mutableListOf<CapacityException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityExceptionStore(): CapacityExceptionStore {
    return MemoryCapacityExceptionStore(mutableMapOf<String, CapacityException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityExceptionStore(
    var inner: CapacityExceptionStore,
    var cache: TtlCache<CapacityException>,
    var hits: Int,
    var misses: Int,
) : CapacityExceptionStore {
    override fun find(code: String): CapacityException? {
        val hit = (cache.get(code) as CapacityException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityException> = inner.list()
}

fun newCachedCapacityExceptionStore(inner: CapacityExceptionStore, ttl: Int, clock: Clock): CapacityExceptionStore {
    return CachedCapacityExceptionStore(
        inner = inner,
        cache = newTtlCache<CapacityException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity exceptions. Empty fields match all. */
data class CapacityExceptionQuery(
    var depot: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityException): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityException>): MutableList<CapacityException> {
        val matched = filter(items) { candidate: CapacityException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityExceptions(store: CapacityExceptionStore, query: CapacityExceptionQuery): MutableList<CapacityException> {
    return query.apply(store.list())
}
