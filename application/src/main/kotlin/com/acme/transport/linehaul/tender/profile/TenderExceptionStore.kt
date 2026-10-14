package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender exceptions by code. */
interface TenderExceptionStore {
    fun find(code: String): TenderException?
    fun save(item: TenderException)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderExceptionStore(
    var items: MutableMap<String, TenderException>,
    var order: MutableList<String>,
) : TenderExceptionStore {
    override fun find(code: String): TenderException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderException) {
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

    override fun list(): MutableList<TenderException> {
        val out = mutableListOf<TenderException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderExceptionStore(): TenderExceptionStore {
    return MemoryTenderExceptionStore(mutableMapOf<String, TenderException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderExceptionStore(
    var inner: TenderExceptionStore,
    var cache: TtlCache<TenderException>,
    var hits: Int,
    var misses: Int,
) : TenderExceptionStore {
    override fun find(code: String): TenderException? {
        val hit = (cache.get(code) as TenderException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderException> = inner.list()
}

fun newCachedTenderExceptionStore(inner: TenderExceptionStore, ttl: Int, clock: Clock): TenderExceptionStore {
    return CachedTenderExceptionStore(
        inner = inner,
        cache = newTtlCache<TenderException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender exceptions. Empty fields match all. */
data class TenderExceptionQuery(
    var segment: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderException): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderException>): MutableList<TenderException> {
        val matched = filter(items) { candidate: TenderException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderExceptions(store: TenderExceptionStore, query: TenderExceptionQuery): MutableList<TenderException> {
    return query.apply(store.list())
}
