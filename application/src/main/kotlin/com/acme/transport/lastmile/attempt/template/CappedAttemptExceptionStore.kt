package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped attempt exceptions by code. */
interface CappedAttemptExceptionStore {
    fun find(code: String): CappedAttemptException?
    fun save(item: CappedAttemptException)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedAttemptException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedAttemptExceptionStore(
    var items: MutableMap<String, CappedAttemptException>,
    var order: MutableList<String>,
) : CappedAttemptExceptionStore {
    override fun find(code: String): CappedAttemptException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedAttemptException) {
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

    override fun list(): MutableList<CappedAttemptException> {
        val out = mutableListOf<CappedAttemptException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedAttemptExceptionStore(): CappedAttemptExceptionStore {
    return MemoryCappedAttemptExceptionStore(
        mutableMapOf<String, CappedAttemptException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedAttemptExceptionStore(
    var inner: CappedAttemptExceptionStore,
    var cache: TtlCache<CappedAttemptException>,
    var hits: Int,
    var misses: Int,
) : CappedAttemptExceptionStore {
    override fun find(code: String): CappedAttemptException? {
        val hit = (cache.get(code) as CappedAttemptException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedAttemptException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedAttemptException> = inner.list()
}

fun newCachedCappedAttemptExceptionStore(inner: CappedAttemptExceptionStore, ttl: Int, clock: Clock): CappedAttemptExceptionStore {
    return CachedCappedAttemptExceptionStore(
        inner = inner,
        cache = newTtlCache<CappedAttemptException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped attempt exceptions. Empty fields match all. */
data class CappedAttemptExceptionQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedAttemptException): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedAttemptException>): MutableList<CappedAttemptException> {
        val matched = filter(items) { candidate: CappedAttemptException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedAttemptExceptions(store: CappedAttemptExceptionStore, query: CappedAttemptExceptionQuery): MutableList<CappedAttemptException> {
    return query.apply(store.list())
}
