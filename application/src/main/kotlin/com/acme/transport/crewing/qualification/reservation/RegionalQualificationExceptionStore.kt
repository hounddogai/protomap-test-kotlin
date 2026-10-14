package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional qualification exceptions by code. */
interface RegionalQualificationExceptionStore {
    fun find(code: String): RegionalQualificationException?
    fun save(item: RegionalQualificationException)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalQualificationException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalQualificationExceptionStore(
    var items: MutableMap<String, RegionalQualificationException>,
    var order: MutableList<String>,
) : RegionalQualificationExceptionStore {
    override fun find(code: String): RegionalQualificationException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalQualificationException) {
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

    override fun list(): MutableList<RegionalQualificationException> {
        val out = mutableListOf<RegionalQualificationException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalQualificationExceptionStore(): RegionalQualificationExceptionStore {
    return MemoryRegionalQualificationExceptionStore(
        mutableMapOf<String, RegionalQualificationException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalQualificationExceptionStore(
    var inner: RegionalQualificationExceptionStore,
    var cache: TtlCache<RegionalQualificationException>,
    var hits: Int,
    var misses: Int,
) : RegionalQualificationExceptionStore {
    override fun find(code: String): RegionalQualificationException? {
        val hit = (cache.get(code) as RegionalQualificationException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalQualificationException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalQualificationException> = inner.list()
}

fun newCachedRegionalQualificationExceptionStore(inner: RegionalQualificationExceptionStore, ttl: Int, clock: Clock): RegionalQualificationExceptionStore {
    return CachedRegionalQualificationExceptionStore(
        inner = inner,
        cache = newTtlCache<RegionalQualificationException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional qualification exceptions. Empty fields match all. */
data class RegionalQualificationExceptionQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalQualificationException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalQualificationException>): MutableList<RegionalQualificationException> {
        val matched = filter(items) { candidate: RegionalQualificationException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalQualificationExceptions(store: RegionalQualificationExceptionStore, query: RegionalQualificationExceptionQuery): MutableList<RegionalQualificationException> {
    return query.apply(store.list())
}
