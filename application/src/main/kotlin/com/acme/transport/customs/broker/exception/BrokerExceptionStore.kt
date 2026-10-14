package com.acme.transport.customs.broker.exception

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker exceptions by code. */
interface BrokerExceptionStore {
    fun find(code: String): BrokerException?
    fun save(item: BrokerException)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerExceptionStore(
    var items: MutableMap<String, BrokerException>,
    var order: MutableList<String>,
) : BrokerExceptionStore {
    override fun find(code: String): BrokerException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerException) {
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

    override fun list(): MutableList<BrokerException> {
        val out = mutableListOf<BrokerException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerExceptionStore(): BrokerExceptionStore {
    return MemoryBrokerExceptionStore(mutableMapOf<String, BrokerException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerExceptionStore(
    var inner: BrokerExceptionStore,
    var cache: TtlCache<BrokerException>,
    var hits: Int,
    var misses: Int,
) : BrokerExceptionStore {
    override fun find(code: String): BrokerException? {
        val hit = (cache.get(code) as BrokerException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerException> = inner.list()
}

fun newCachedBrokerExceptionStore(inner: BrokerExceptionStore, ttl: Int, clock: Clock): BrokerExceptionStore {
    return CachedBrokerExceptionStore(
        inner = inner,
        cache = newTtlCache<BrokerException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker exceptions. Empty fields match all. */
data class BrokerExceptionQuery(
    var segment: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerException): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerException>): MutableList<BrokerException> {
        val matched = filter(items) { candidate: BrokerException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerExceptions(store: BrokerExceptionStore, query: BrokerExceptionQuery): MutableList<BrokerException> {
    return query.apply(store.list())
}
