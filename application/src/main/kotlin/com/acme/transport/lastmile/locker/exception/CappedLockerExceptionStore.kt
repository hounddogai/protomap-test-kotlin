package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped locker exceptions by code. */
interface CappedLockerExceptionStore {
    fun find(code: String): CappedLockerException?
    fun save(item: CappedLockerException)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedLockerException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedLockerExceptionStore(
    var items: MutableMap<String, CappedLockerException>,
    var order: MutableList<String>,
) : CappedLockerExceptionStore {
    override fun find(code: String): CappedLockerException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedLockerException) {
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

    override fun list(): MutableList<CappedLockerException> {
        val out = mutableListOf<CappedLockerException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedLockerExceptionStore(): CappedLockerExceptionStore {
    return MemoryCappedLockerExceptionStore(
        mutableMapOf<String, CappedLockerException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedLockerExceptionStore(
    var inner: CappedLockerExceptionStore,
    var cache: TtlCache<CappedLockerException>,
    var hits: Int,
    var misses: Int,
) : CappedLockerExceptionStore {
    override fun find(code: String): CappedLockerException? {
        val hit = (cache.get(code) as CappedLockerException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedLockerException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedLockerException> = inner.list()
}

fun newCachedCappedLockerExceptionStore(inner: CappedLockerExceptionStore, ttl: Int, clock: Clock): CappedLockerExceptionStore {
    return CachedCappedLockerExceptionStore(
        inner = inner,
        cache = newTtlCache<CappedLockerException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped locker exceptions. Empty fields match all. */
data class CappedLockerExceptionQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedLockerException): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedLockerException>): MutableList<CappedLockerException> {
        val matched = filter(items) { candidate: CappedLockerException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedLockerExceptions(store: CappedLockerExceptionStore, query: CappedLockerExceptionQuery): MutableList<CappedLockerException> {
    return query.apply(store.list())
}
