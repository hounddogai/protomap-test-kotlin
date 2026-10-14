package com.acme.transport.freightpay.dispute.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered dispute exceptions by code. */
interface TieredDisputeExceptionStore {
    fun find(code: String): TieredDisputeException?
    fun save(item: TieredDisputeException)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDisputeException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDisputeExceptionStore(
    var items: MutableMap<String, TieredDisputeException>,
    var order: MutableList<String>,
) : TieredDisputeExceptionStore {
    override fun find(code: String): TieredDisputeException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDisputeException) {
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

    override fun list(): MutableList<TieredDisputeException> {
        val out = mutableListOf<TieredDisputeException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDisputeExceptionStore(): TieredDisputeExceptionStore {
    return MemoryTieredDisputeExceptionStore(
        mutableMapOf<String, TieredDisputeException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDisputeExceptionStore(
    var inner: TieredDisputeExceptionStore,
    var cache: TtlCache<TieredDisputeException>,
    var hits: Int,
    var misses: Int,
) : TieredDisputeExceptionStore {
    override fun find(code: String): TieredDisputeException? {
        val hit = (cache.get(code) as TieredDisputeException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDisputeException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDisputeException> = inner.list()
}

fun newCachedTieredDisputeExceptionStore(inner: TieredDisputeExceptionStore, ttl: Int, clock: Clock): TieredDisputeExceptionStore {
    return CachedTieredDisputeExceptionStore(
        inner = inner,
        cache = newTtlCache<TieredDisputeException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered dispute exceptions. Empty fields match all. */
data class TieredDisputeExceptionQuery(
    var unit: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDisputeException): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<TieredDisputeException>): MutableList<TieredDisputeException> {
        val matched = filter(items) { candidate: TieredDisputeException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDisputeExceptions(store: TieredDisputeExceptionStore, query: TieredDisputeExceptionQuery): MutableList<TieredDisputeException> {
    return query.apply(store.list())
}
