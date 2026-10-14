package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted drop exceptions by code. */
interface WeightedDropExceptionStore {
    fun find(code: String): WeightedDropException?
    fun save(item: WeightedDropException)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDropException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDropExceptionStore(
    var items: MutableMap<String, WeightedDropException>,
    var order: MutableList<String>,
) : WeightedDropExceptionStore {
    override fun find(code: String): WeightedDropException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDropException) {
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

    override fun list(): MutableList<WeightedDropException> {
        val out = mutableListOf<WeightedDropException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDropExceptionStore(): WeightedDropExceptionStore {
    return MemoryWeightedDropExceptionStore(
        mutableMapOf<String, WeightedDropException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDropExceptionStore(
    var inner: WeightedDropExceptionStore,
    var cache: TtlCache<WeightedDropException>,
    var hits: Int,
    var misses: Int,
) : WeightedDropExceptionStore {
    override fun find(code: String): WeightedDropException? {
        val hit = (cache.get(code) as WeightedDropException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDropException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDropException> = inner.list()
}

fun newCachedWeightedDropExceptionStore(inner: WeightedDropExceptionStore, ttl: Int, clock: Clock): WeightedDropExceptionStore {
    return CachedWeightedDropExceptionStore(
        inner = inner,
        cache = newTtlCache<WeightedDropException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted drop exceptions. Empty fields match all. */
data class WeightedDropExceptionQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDropException): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<WeightedDropException>): MutableList<WeightedDropException> {
        val matched = filter(items) { candidate: WeightedDropException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDropExceptions(store: WeightedDropExceptionStore, query: WeightedDropExceptionQuery): MutableList<WeightedDropException> {
    return query.apply(store.list())
}
