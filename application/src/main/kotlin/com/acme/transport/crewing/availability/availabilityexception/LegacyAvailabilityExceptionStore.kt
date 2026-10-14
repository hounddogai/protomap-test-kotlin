package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy availability exceptions by code. */
interface LegacyAvailabilityExceptionStore {
    fun find(code: String): LegacyAvailabilityException?
    fun save(item: LegacyAvailabilityException)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAvailabilityException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAvailabilityExceptionStore(
    var items: MutableMap<String, LegacyAvailabilityException>,
    var order: MutableList<String>,
) : LegacyAvailabilityExceptionStore {
    override fun find(code: String): LegacyAvailabilityException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAvailabilityException) {
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

    override fun list(): MutableList<LegacyAvailabilityException> {
        val out = mutableListOf<LegacyAvailabilityException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAvailabilityExceptionStore(): LegacyAvailabilityExceptionStore {
    return MemoryLegacyAvailabilityExceptionStore(
        mutableMapOf<String, LegacyAvailabilityException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAvailabilityExceptionStore(
    var inner: LegacyAvailabilityExceptionStore,
    var cache: TtlCache<LegacyAvailabilityException>,
    var hits: Int,
    var misses: Int,
) : LegacyAvailabilityExceptionStore {
    override fun find(code: String): LegacyAvailabilityException? {
        val hit = (cache.get(code) as LegacyAvailabilityException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAvailabilityException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAvailabilityException> = inner.list()
}

fun newCachedLegacyAvailabilityExceptionStore(inner: LegacyAvailabilityExceptionStore, ttl: Int, clock: Clock): LegacyAvailabilityExceptionStore {
    return CachedLegacyAvailabilityExceptionStore(
        inner = inner,
        cache = newTtlCache<LegacyAvailabilityException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy availability exceptions. Empty fields match all. */
data class LegacyAvailabilityExceptionQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAvailabilityException): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyAvailabilityException>): MutableList<LegacyAvailabilityException> {
        val matched = filter(items) { candidate: LegacyAvailabilityException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAvailabilityExceptions(store: LegacyAvailabilityExceptionStore, query: LegacyAvailabilityExceptionQuery): MutableList<LegacyAvailabilityException> {
    return query.apply(store.list())
}
