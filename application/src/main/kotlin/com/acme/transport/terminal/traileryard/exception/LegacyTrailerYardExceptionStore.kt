package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy trailer yard exceptions by code. */
interface LegacyTrailerYardExceptionStore {
    fun find(code: String): LegacyTrailerYardException?
    fun save(item: LegacyTrailerYardException)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTrailerYardException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTrailerYardExceptionStore(
    var items: MutableMap<String, LegacyTrailerYardException>,
    var order: MutableList<String>,
) : LegacyTrailerYardExceptionStore {
    override fun find(code: String): LegacyTrailerYardException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTrailerYardException) {
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

    override fun list(): MutableList<LegacyTrailerYardException> {
        val out = mutableListOf<LegacyTrailerYardException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTrailerYardExceptionStore(): LegacyTrailerYardExceptionStore {
    return MemoryLegacyTrailerYardExceptionStore(
        mutableMapOf<String, LegacyTrailerYardException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTrailerYardExceptionStore(
    var inner: LegacyTrailerYardExceptionStore,
    var cache: TtlCache<LegacyTrailerYardException>,
    var hits: Int,
    var misses: Int,
) : LegacyTrailerYardExceptionStore {
    override fun find(code: String): LegacyTrailerYardException? {
        val hit = (cache.get(code) as LegacyTrailerYardException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTrailerYardException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTrailerYardException> = inner.list()
}

fun newCachedLegacyTrailerYardExceptionStore(inner: LegacyTrailerYardExceptionStore, ttl: Int, clock: Clock): LegacyTrailerYardExceptionStore {
    return CachedLegacyTrailerYardExceptionStore(
        inner = inner,
        cache = newTtlCache<LegacyTrailerYardException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy trailer yard exceptions. Empty fields match all. */
data class LegacyTrailerYardExceptionQuery(
    var segment: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTrailerYardException): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyTrailerYardException>): MutableList<LegacyTrailerYardException> {
        val matched = filter(items) { candidate: LegacyTrailerYardException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTrailerYardExceptions(store: LegacyTrailerYardExceptionStore, query: LegacyTrailerYardExceptionQuery): MutableList<LegacyTrailerYardException> {
    return query.apply(store.list())
}
