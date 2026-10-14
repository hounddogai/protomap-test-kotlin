package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient trailer variances by code. */
interface LenientTrailerVarianceStore {
    fun find(code: String): LenientTrailerVariance?
    fun save(item: LenientTrailerVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientTrailerVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientTrailerVarianceStore(
    var items: MutableMap<String, LenientTrailerVariance>,
    var order: MutableList<String>,
) : LenientTrailerVarianceStore {
    override fun find(code: String): LenientTrailerVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientTrailerVariance) {
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

    override fun list(): MutableList<LenientTrailerVariance> {
        val out = mutableListOf<LenientTrailerVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientTrailerVarianceStore(): LenientTrailerVarianceStore {
    return MemoryLenientTrailerVarianceStore(
        mutableMapOf<String, LenientTrailerVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientTrailerVarianceStore(
    var inner: LenientTrailerVarianceStore,
    var cache: TtlCache<LenientTrailerVariance>,
    var hits: Int,
    var misses: Int,
) : LenientTrailerVarianceStore {
    override fun find(code: String): LenientTrailerVariance? {
        val hit = (cache.get(code) as LenientTrailerVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientTrailerVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientTrailerVariance> = inner.list()
}

fun newCachedLenientTrailerVarianceStore(inner: LenientTrailerVarianceStore, ttl: Int, clock: Clock): LenientTrailerVarianceStore {
    return CachedLenientTrailerVarianceStore(
        inner = inner,
        cache = newTtlCache<LenientTrailerVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient trailer variances. Empty fields match all. */
data class LenientTrailerVarianceQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientTrailerVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientTrailerVariance>): MutableList<LenientTrailerVariance> {
        val matched = filter(items) { candidate: LenientTrailerVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientTrailerVariances(store: LenientTrailerVarianceStore, query: LenientTrailerVarianceQuery): MutableList<LenientTrailerVariance> {
    return query.apply(store.list())
}
