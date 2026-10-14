package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection exceptions by code. */
interface InspectionExceptionStore {
    fun find(code: String): InspectionException?
    fun save(item: InspectionException)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionExceptionStore(
    var items: MutableMap<String, InspectionException>,
    var order: MutableList<String>,
) : InspectionExceptionStore {
    override fun find(code: String): InspectionException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionException) {
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

    override fun list(): MutableList<InspectionException> {
        val out = mutableListOf<InspectionException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionExceptionStore(): InspectionExceptionStore {
    return MemoryInspectionExceptionStore(mutableMapOf<String, InspectionException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionExceptionStore(
    var inner: InspectionExceptionStore,
    var cache: TtlCache<InspectionException>,
    var hits: Int,
    var misses: Int,
) : InspectionExceptionStore {
    override fun find(code: String): InspectionException? {
        val hit = (cache.get(code) as InspectionException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionException> = inner.list()
}

fun newCachedInspectionExceptionStore(inner: InspectionExceptionStore, ttl: Int, clock: Clock): InspectionExceptionStore {
    return CachedInspectionExceptionStore(
        inner = inner,
        cache = newTtlCache<InspectionException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection exceptions. Empty fields match all. */
data class InspectionExceptionQuery(
    var locale: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionException): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionException>): MutableList<InspectionException> {
        val matched = filter(items) { candidate: InspectionException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionExceptions(store: InspectionExceptionStore, query: InspectionExceptionQuery): MutableList<InspectionException> {
    return query.apply(store.list())
}
