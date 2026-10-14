package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient violation exceptions by code. */
interface LenientViolationExceptionStore {
    fun find(code: String): LenientViolationException?
    fun save(item: LenientViolationException)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientViolationException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientViolationExceptionStore(
    var items: MutableMap<String, LenientViolationException>,
    var order: MutableList<String>,
) : LenientViolationExceptionStore {
    override fun find(code: String): LenientViolationException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientViolationException) {
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

    override fun list(): MutableList<LenientViolationException> {
        val out = mutableListOf<LenientViolationException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientViolationExceptionStore(): LenientViolationExceptionStore {
    return MemoryLenientViolationExceptionStore(
        mutableMapOf<String, LenientViolationException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientViolationExceptionStore(
    var inner: LenientViolationExceptionStore,
    var cache: TtlCache<LenientViolationException>,
    var hits: Int,
    var misses: Int,
) : LenientViolationExceptionStore {
    override fun find(code: String): LenientViolationException? {
        val hit = (cache.get(code) as LenientViolationException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientViolationException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientViolationException> = inner.list()
}

fun newCachedLenientViolationExceptionStore(inner: LenientViolationExceptionStore, ttl: Int, clock: Clock): LenientViolationExceptionStore {
    return CachedLenientViolationExceptionStore(
        inner = inner,
        cache = newTtlCache<LenientViolationException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient violation exceptions. Empty fields match all. */
data class LenientViolationExceptionQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientViolationException): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientViolationException>): MutableList<LenientViolationException> {
        val matched = filter(items) { candidate: LenientViolationException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientViolationExceptions(store: LenientViolationExceptionStore, query: LenientViolationExceptionQuery): MutableList<LenientViolationException> {
    return query.apply(store.list())
}
