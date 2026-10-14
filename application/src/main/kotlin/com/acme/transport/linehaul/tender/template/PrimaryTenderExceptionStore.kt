package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary tender exceptions by code. */
interface PrimaryTenderExceptionStore {
    fun find(code: String): PrimaryTenderException?
    fun save(item: PrimaryTenderException)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTenderException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTenderExceptionStore(
    var items: MutableMap<String, PrimaryTenderException>,
    var order: MutableList<String>,
) : PrimaryTenderExceptionStore {
    override fun find(code: String): PrimaryTenderException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTenderException) {
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

    override fun list(): MutableList<PrimaryTenderException> {
        val out = mutableListOf<PrimaryTenderException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTenderExceptionStore(): PrimaryTenderExceptionStore {
    return MemoryPrimaryTenderExceptionStore(
        mutableMapOf<String, PrimaryTenderException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTenderExceptionStore(
    var inner: PrimaryTenderExceptionStore,
    var cache: TtlCache<PrimaryTenderException>,
    var hits: Int,
    var misses: Int,
) : PrimaryTenderExceptionStore {
    override fun find(code: String): PrimaryTenderException? {
        val hit = (cache.get(code) as PrimaryTenderException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTenderException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTenderException> = inner.list()
}

fun newCachedPrimaryTenderExceptionStore(inner: PrimaryTenderExceptionStore, ttl: Int, clock: Clock): PrimaryTenderExceptionStore {
    return CachedPrimaryTenderExceptionStore(
        inner = inner,
        cache = newTtlCache<PrimaryTenderException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tender exceptions. Empty fields match all. */
data class PrimaryTenderExceptionQuery(
    var country: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTenderException): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTenderException>): MutableList<PrimaryTenderException> {
        val matched = filter(items) { candidate: PrimaryTenderException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTenderExceptions(store: PrimaryTenderExceptionStore, query: PrimaryTenderExceptionQuery): MutableList<PrimaryTenderException> {
    return query.apply(store.list())
}
