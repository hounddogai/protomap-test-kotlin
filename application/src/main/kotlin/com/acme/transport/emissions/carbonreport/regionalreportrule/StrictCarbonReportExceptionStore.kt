package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict carbon report exceptions by code. */
interface StrictCarbonReportExceptionStore {
    fun find(code: String): StrictCarbonReportException?
    fun save(item: StrictCarbonReportException)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCarbonReportException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCarbonReportExceptionStore(
    var items: MutableMap<String, StrictCarbonReportException>,
    var order: MutableList<String>,
) : StrictCarbonReportExceptionStore {
    override fun find(code: String): StrictCarbonReportException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCarbonReportException) {
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

    override fun list(): MutableList<StrictCarbonReportException> {
        val out = mutableListOf<StrictCarbonReportException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCarbonReportExceptionStore(): StrictCarbonReportExceptionStore {
    return MemoryStrictCarbonReportExceptionStore(
        mutableMapOf<String, StrictCarbonReportException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCarbonReportExceptionStore(
    var inner: StrictCarbonReportExceptionStore,
    var cache: TtlCache<StrictCarbonReportException>,
    var hits: Int,
    var misses: Int,
) : StrictCarbonReportExceptionStore {
    override fun find(code: String): StrictCarbonReportException? {
        val hit = (cache.get(code) as StrictCarbonReportException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCarbonReportException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCarbonReportException> = inner.list()
}

fun newCachedStrictCarbonReportExceptionStore(inner: StrictCarbonReportExceptionStore, ttl: Int, clock: Clock): StrictCarbonReportExceptionStore {
    return CachedStrictCarbonReportExceptionStore(
        inner = inner,
        cache = newTtlCache<StrictCarbonReportException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict carbon report exceptions. Empty fields match all. */
data class StrictCarbonReportExceptionQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCarbonReportException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictCarbonReportException>): MutableList<StrictCarbonReportException> {
        val matched = filter(items) { candidate: StrictCarbonReportException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCarbonReportExceptions(store: StrictCarbonReportExceptionStore, query: StrictCarbonReportExceptionQuery): MutableList<StrictCarbonReportException> {
    return query.apply(store.list())
}
