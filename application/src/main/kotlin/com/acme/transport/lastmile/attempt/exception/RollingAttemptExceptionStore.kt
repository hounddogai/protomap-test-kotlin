package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling attempt exceptions by code. */
interface RollingAttemptExceptionStore {
    fun find(code: String): RollingAttemptException?
    fun save(item: RollingAttemptException)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAttemptException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAttemptExceptionStore(
    var items: MutableMap<String, RollingAttemptException>,
    var order: MutableList<String>,
) : RollingAttemptExceptionStore {
    override fun find(code: String): RollingAttemptException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAttemptException) {
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

    override fun list(): MutableList<RollingAttemptException> {
        val out = mutableListOf<RollingAttemptException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAttemptExceptionStore(): RollingAttemptExceptionStore {
    return MemoryRollingAttemptExceptionStore(
        mutableMapOf<String, RollingAttemptException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAttemptExceptionStore(
    var inner: RollingAttemptExceptionStore,
    var cache: TtlCache<RollingAttemptException>,
    var hits: Int,
    var misses: Int,
) : RollingAttemptExceptionStore {
    override fun find(code: String): RollingAttemptException? {
        val hit = (cache.get(code) as RollingAttemptException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAttemptException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAttemptException> = inner.list()
}

fun newCachedRollingAttemptExceptionStore(inner: RollingAttemptExceptionStore, ttl: Int, clock: Clock): RollingAttemptExceptionStore {
    return CachedRollingAttemptExceptionStore(
        inner = inner,
        cache = newTtlCache<RollingAttemptException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling attempt exceptions. Empty fields match all. */
data class RollingAttemptExceptionQuery(
    var category: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAttemptException): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAttemptException>): MutableList<RollingAttemptException> {
        val matched = filter(items) { candidate: RollingAttemptException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAttemptExceptions(store: RollingAttemptExceptionStore, query: RollingAttemptExceptionQuery): MutableList<RollingAttemptException> {
    return query.apply(store.list())
}
