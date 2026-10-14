package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled trailer exceptions by code. */
interface ScheduledTrailerExceptionStore {
    fun find(code: String): ScheduledTrailerException?
    fun save(item: ScheduledTrailerException)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTrailerException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTrailerExceptionStore(
    var items: MutableMap<String, ScheduledTrailerException>,
    var order: MutableList<String>,
) : ScheduledTrailerExceptionStore {
    override fun find(code: String): ScheduledTrailerException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTrailerException) {
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

    override fun list(): MutableList<ScheduledTrailerException> {
        val out = mutableListOf<ScheduledTrailerException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTrailerExceptionStore(): ScheduledTrailerExceptionStore {
    return MemoryScheduledTrailerExceptionStore(
        mutableMapOf<String, ScheduledTrailerException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTrailerExceptionStore(
    var inner: ScheduledTrailerExceptionStore,
    var cache: TtlCache<ScheduledTrailerException>,
    var hits: Int,
    var misses: Int,
) : ScheduledTrailerExceptionStore {
    override fun find(code: String): ScheduledTrailerException? {
        val hit = (cache.get(code) as ScheduledTrailerException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTrailerException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTrailerException> = inner.list()
}

fun newCachedScheduledTrailerExceptionStore(inner: ScheduledTrailerExceptionStore, ttl: Int, clock: Clock): ScheduledTrailerExceptionStore {
    return CachedScheduledTrailerExceptionStore(
        inner = inner,
        cache = newTtlCache<ScheduledTrailerException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled trailer exceptions. Empty fields match all. */
data class ScheduledTrailerExceptionQuery(
    var unit: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTrailerException): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ScheduledTrailerException>): MutableList<ScheduledTrailerException> {
        val matched = filter(items) { candidate: ScheduledTrailerException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTrailerExceptions(store: ScheduledTrailerExceptionStore, query: ScheduledTrailerExceptionQuery): MutableList<ScheduledTrailerException> {
    return query.apply(store.list())
}
