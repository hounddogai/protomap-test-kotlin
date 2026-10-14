package com.acme.transport.fleetops.inspection.score

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary inspection exceptions by code. */
interface PrimaryInspectionExceptionStore {
    fun find(code: String): PrimaryInspectionException?
    fun save(item: PrimaryInspectionException)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryInspectionException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryInspectionExceptionStore(
    var items: MutableMap<String, PrimaryInspectionException>,
    var order: MutableList<String>,
) : PrimaryInspectionExceptionStore {
    override fun find(code: String): PrimaryInspectionException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryInspectionException) {
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

    override fun list(): MutableList<PrimaryInspectionException> {
        val out = mutableListOf<PrimaryInspectionException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryInspectionExceptionStore(): PrimaryInspectionExceptionStore {
    return MemoryPrimaryInspectionExceptionStore(
        mutableMapOf<String, PrimaryInspectionException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryInspectionExceptionStore(
    var inner: PrimaryInspectionExceptionStore,
    var cache: TtlCache<PrimaryInspectionException>,
    var hits: Int,
    var misses: Int,
) : PrimaryInspectionExceptionStore {
    override fun find(code: String): PrimaryInspectionException? {
        val hit = (cache.get(code) as PrimaryInspectionException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryInspectionException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryInspectionException> = inner.list()
}

fun newCachedPrimaryInspectionExceptionStore(inner: PrimaryInspectionExceptionStore, ttl: Int, clock: Clock): PrimaryInspectionExceptionStore {
    return CachedPrimaryInspectionExceptionStore(
        inner = inner,
        cache = newTtlCache<PrimaryInspectionException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary inspection exceptions. Empty fields match all. */
data class PrimaryInspectionExceptionQuery(
    var locale: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryInspectionException): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<PrimaryInspectionException>): MutableList<PrimaryInspectionException> {
        val matched = filter(items) { candidate: PrimaryInspectionException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryInspectionExceptions(store: PrimaryInspectionExceptionStore, query: PrimaryInspectionExceptionQuery): MutableList<PrimaryInspectionException> {
    return query.apply(store.list())
}
