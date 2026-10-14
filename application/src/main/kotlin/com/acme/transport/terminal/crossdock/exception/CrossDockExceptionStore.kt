package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock exceptions by code. */
interface CrossDockExceptionStore {
    fun find(code: String): CrossDockException?
    fun save(item: CrossDockException)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockExceptionStore(
    var items: MutableMap<String, CrossDockException>,
    var order: MutableList<String>,
) : CrossDockExceptionStore {
    override fun find(code: String): CrossDockException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockException) {
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

    override fun list(): MutableList<CrossDockException> {
        val out = mutableListOf<CrossDockException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockExceptionStore(): CrossDockExceptionStore {
    return MemoryCrossDockExceptionStore(mutableMapOf<String, CrossDockException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockExceptionStore(
    var inner: CrossDockExceptionStore,
    var cache: TtlCache<CrossDockException>,
    var hits: Int,
    var misses: Int,
) : CrossDockExceptionStore {
    override fun find(code: String): CrossDockException? {
        val hit = (cache.get(code) as CrossDockException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockException> = inner.list()
}

fun newCachedCrossDockExceptionStore(inner: CrossDockExceptionStore, ttl: Int, clock: Clock): CrossDockExceptionStore {
    return CachedCrossDockExceptionStore(
        inner = inner,
        cache = newTtlCache<CrossDockException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock exceptions. Empty fields match all. */
data class CrossDockExceptionQuery(
    var source: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockException): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockException>): MutableList<CrossDockException> {
        val matched = filter(items) { candidate: CrossDockException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockExceptions(store: CrossDockExceptionStore, query: CrossDockExceptionQuery): MutableList<CrossDockException> {
    return query.apply(store.list())
}
