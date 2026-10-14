package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default capacity exceptions by code. */
interface DefaultCapacityExceptionStore {
    fun find(code: String): DefaultCapacityException?
    fun save(item: DefaultCapacityException)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCapacityException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCapacityExceptionStore(
    var items: MutableMap<String, DefaultCapacityException>,
    var order: MutableList<String>,
) : DefaultCapacityExceptionStore {
    override fun find(code: String): DefaultCapacityException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCapacityException) {
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

    override fun list(): MutableList<DefaultCapacityException> {
        val out = mutableListOf<DefaultCapacityException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCapacityExceptionStore(): DefaultCapacityExceptionStore {
    return MemoryDefaultCapacityExceptionStore(
        mutableMapOf<String, DefaultCapacityException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCapacityExceptionStore(
    var inner: DefaultCapacityExceptionStore,
    var cache: TtlCache<DefaultCapacityException>,
    var hits: Int,
    var misses: Int,
) : DefaultCapacityExceptionStore {
    override fun find(code: String): DefaultCapacityException? {
        val hit = (cache.get(code) as DefaultCapacityException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCapacityException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCapacityException> = inner.list()
}

fun newCachedDefaultCapacityExceptionStore(inner: DefaultCapacityExceptionStore, ttl: Int, clock: Clock): DefaultCapacityExceptionStore {
    return CachedDefaultCapacityExceptionStore(
        inner = inner,
        cache = newTtlCache<DefaultCapacityException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default capacity exceptions. Empty fields match all. */
data class DefaultCapacityExceptionQuery(
    var source: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCapacityException): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCapacityException>): MutableList<DefaultCapacityException> {
        val matched = filter(items) { candidate: DefaultCapacityException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCapacityExceptions(store: DefaultCapacityExceptionStore, query: DefaultCapacityExceptionQuery): MutableList<DefaultCapacityException> {
    return query.apply(store.list())
}
