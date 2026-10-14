package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient locker exceptions by code. */
interface LenientLockerExceptionStore {
    fun find(code: String): LenientLockerException?
    fun save(item: LenientLockerException)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientLockerException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientLockerExceptionStore(
    var items: MutableMap<String, LenientLockerException>,
    var order: MutableList<String>,
) : LenientLockerExceptionStore {
    override fun find(code: String): LenientLockerException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientLockerException) {
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

    override fun list(): MutableList<LenientLockerException> {
        val out = mutableListOf<LenientLockerException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientLockerExceptionStore(): LenientLockerExceptionStore {
    return MemoryLenientLockerExceptionStore(
        mutableMapOf<String, LenientLockerException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientLockerExceptionStore(
    var inner: LenientLockerExceptionStore,
    var cache: TtlCache<LenientLockerException>,
    var hits: Int,
    var misses: Int,
) : LenientLockerExceptionStore {
    override fun find(code: String): LenientLockerException? {
        val hit = (cache.get(code) as LenientLockerException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientLockerException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientLockerException> = inner.list()
}

fun newCachedLenientLockerExceptionStore(inner: LenientLockerExceptionStore, ttl: Int, clock: Clock): LenientLockerExceptionStore {
    return CachedLenientLockerExceptionStore(
        inner = inner,
        cache = newTtlCache<LenientLockerException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient locker exceptions. Empty fields match all. */
data class LenientLockerExceptionQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientLockerException): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<LenientLockerException>): MutableList<LenientLockerException> {
        val matched = filter(items) { candidate: LenientLockerException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientLockerExceptions(store: LenientLockerExceptionStore, query: LenientLockerExceptionQuery): MutableList<LenientLockerException> {
    return query.apply(store.list())
}
