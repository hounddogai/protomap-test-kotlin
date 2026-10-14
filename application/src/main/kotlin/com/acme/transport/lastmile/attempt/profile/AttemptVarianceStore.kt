package com.acme.transport.lastmile.attempt.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt variances by code. */
interface AttemptVarianceStore {
    fun find(code: String): AttemptVariance?
    fun save(item: AttemptVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptVarianceStore(
    var items: MutableMap<String, AttemptVariance>,
    var order: MutableList<String>,
) : AttemptVarianceStore {
    override fun find(code: String): AttemptVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptVariance) {
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

    override fun list(): MutableList<AttemptVariance> {
        val out = mutableListOf<AttemptVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptVarianceStore(): AttemptVarianceStore {
    return MemoryAttemptVarianceStore(mutableMapOf<String, AttemptVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptVarianceStore(
    var inner: AttemptVarianceStore,
    var cache: TtlCache<AttemptVariance>,
    var hits: Int,
    var misses: Int,
) : AttemptVarianceStore {
    override fun find(code: String): AttemptVariance? {
        val hit = (cache.get(code) as AttemptVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptVariance> = inner.list()
}

fun newCachedAttemptVarianceStore(inner: AttemptVarianceStore, ttl: Int, clock: Clock): AttemptVarianceStore {
    return CachedAttemptVarianceStore(
        inner = inner,
        cache = newTtlCache<AttemptVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt variances. Empty fields match all. */
data class AttemptVarianceQuery(
    var locale: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptVariance): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<AttemptVariance>): MutableList<AttemptVariance> {
        val matched = filter(items) { candidate: AttemptVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptVariances(store: AttemptVarianceStore, query: AttemptVarianceQuery): MutableList<AttemptVariance> {
    return query.apply(store.list())
}
