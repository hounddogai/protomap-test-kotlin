package com.acme.transport.customs.hold.batch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold exceptions by code. */
interface HoldExceptionStore {
    fun find(code: String): HoldException?
    fun save(item: HoldException)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldExceptionStore(
    var items: MutableMap<String, HoldException>,
    var order: MutableList<String>,
) : HoldExceptionStore {
    override fun find(code: String): HoldException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldException) {
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

    override fun list(): MutableList<HoldException> {
        val out = mutableListOf<HoldException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldExceptionStore(): HoldExceptionStore {
    return MemoryHoldExceptionStore(mutableMapOf<String, HoldException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldExceptionStore(
    var inner: HoldExceptionStore,
    var cache: TtlCache<HoldException>,
    var hits: Int,
    var misses: Int,
) : HoldExceptionStore {
    override fun find(code: String): HoldException? {
        val hit = (cache.get(code) as HoldException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldException> = inner.list()
}

fun newCachedHoldExceptionStore(inner: HoldExceptionStore, ttl: Int, clock: Clock): HoldExceptionStore {
    return CachedHoldExceptionStore(
        inner = inner,
        cache = newTtlCache<HoldException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold exceptions. Empty fields match all. */
data class HoldExceptionQuery(
    var channel: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldException): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<HoldException>): MutableList<HoldException> {
        val matched = filter(items) { candidate: HoldException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldExceptions(store: HoldExceptionStore, query: HoldExceptionQuery): MutableList<HoldException> {
    return query.apply(store.list())
}
