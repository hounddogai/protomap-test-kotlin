package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification variances by code. */
interface QualificationVarianceStore {
    fun find(code: String): QualificationVariance?
    fun save(item: QualificationVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationVarianceStore(
    var items: MutableMap<String, QualificationVariance>,
    var order: MutableList<String>,
) : QualificationVarianceStore {
    override fun find(code: String): QualificationVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationVariance) {
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

    override fun list(): MutableList<QualificationVariance> {
        val out = mutableListOf<QualificationVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationVarianceStore(): QualificationVarianceStore {
    return MemoryQualificationVarianceStore(
        mutableMapOf<String, QualificationVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationVarianceStore(
    var inner: QualificationVarianceStore,
    var cache: TtlCache<QualificationVariance>,
    var hits: Int,
    var misses: Int,
) : QualificationVarianceStore {
    override fun find(code: String): QualificationVariance? {
        val hit = (cache.get(code) as QualificationVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationVariance> = inner.list()
}

fun newCachedQualificationVarianceStore(inner: QualificationVarianceStore, ttl: Int, clock: Clock): QualificationVarianceStore {
    return CachedQualificationVarianceStore(
        inner = inner,
        cache = newTtlCache<QualificationVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification variances. Empty fields match all. */
data class QualificationVarianceQuery(
    var locale: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationVariance): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationVariance>): MutableList<QualificationVariance> {
        val matched = filter(items) { candidate: QualificationVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationVariances(store: QualificationVarianceStore, query: QualificationVarianceQuery): MutableList<QualificationVariance> {
    return query.apply(store.list())
}
