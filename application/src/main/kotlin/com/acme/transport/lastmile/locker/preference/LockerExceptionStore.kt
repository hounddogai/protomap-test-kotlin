package com.acme.transport.lastmile.locker.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker exceptions by code. */
interface LockerExceptionStore {
    fun find(code: String): LockerException?
    fun save(item: LockerException)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerExceptionStore(
    var items: MutableMap<String, LockerException>,
    var order: MutableList<String>,
) : LockerExceptionStore {
    override fun find(code: String): LockerException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerException) {
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

    override fun list(): MutableList<LockerException> {
        val out = mutableListOf<LockerException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerExceptionStore(): LockerExceptionStore {
    return MemoryLockerExceptionStore(mutableMapOf<String, LockerException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerExceptionStore(
    var inner: LockerExceptionStore,
    var cache: TtlCache<LockerException>,
    var hits: Int,
    var misses: Int,
) : LockerExceptionStore {
    override fun find(code: String): LockerException? {
        val hit = (cache.get(code) as LockerException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerException> = inner.list()
}

fun newCachedLockerExceptionStore(inner: LockerExceptionStore, ttl: Int, clock: Clock): LockerExceptionStore {
    return CachedLockerExceptionStore(
        inner = inner,
        cache = newTtlCache<LockerException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker exceptions. Empty fields match all. */
data class LockerExceptionQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerException): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerException>): MutableList<LockerException> {
        val matched = filter(items) { candidate: LockerException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerExceptions(store: LockerExceptionStore, query: LockerExceptionQuery): MutableList<LockerException> {
    return query.apply(store.list())
}
