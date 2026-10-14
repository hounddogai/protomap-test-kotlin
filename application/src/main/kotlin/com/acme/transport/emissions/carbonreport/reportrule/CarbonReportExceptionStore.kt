package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report exceptions by code. */
interface CarbonReportExceptionStore {
    fun find(code: String): CarbonReportException?
    fun save(item: CarbonReportException)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportExceptionStore(
    var items: MutableMap<String, CarbonReportException>,
    var order: MutableList<String>,
) : CarbonReportExceptionStore {
    override fun find(code: String): CarbonReportException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportException) {
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

    override fun list(): MutableList<CarbonReportException> {
        val out = mutableListOf<CarbonReportException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportExceptionStore(): CarbonReportExceptionStore {
    return MemoryCarbonReportExceptionStore(
        mutableMapOf<String, CarbonReportException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportExceptionStore(
    var inner: CarbonReportExceptionStore,
    var cache: TtlCache<CarbonReportException>,
    var hits: Int,
    var misses: Int,
) : CarbonReportExceptionStore {
    override fun find(code: String): CarbonReportException? {
        val hit = (cache.get(code) as CarbonReportException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportException> = inner.list()
}

fun newCachedCarbonReportExceptionStore(inner: CarbonReportExceptionStore, ttl: Int, clock: Clock): CarbonReportExceptionStore {
    return CachedCarbonReportExceptionStore(
        inner = inner,
        cache = newTtlCache<CarbonReportException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report exceptions. Empty fields match all. */
data class CarbonReportExceptionQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportException>): MutableList<CarbonReportException> {
        val matched = filter(items) { candidate: CarbonReportException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportExceptions(store: CarbonReportExceptionStore, query: CarbonReportExceptionQuery): MutableList<CarbonReportException> {
    return query.apply(store.list())
}
