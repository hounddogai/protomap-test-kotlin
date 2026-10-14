package com.acme.transport.crewing.qualification.estimate

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification estimates by code. */
interface QualificationEstimateStore {
    fun find(code: String): QualificationEstimate?
    fun save(item: QualificationEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationEstimateStore(
    var items: MutableMap<String, QualificationEstimate>,
    var order: MutableList<String>,
) : QualificationEstimateStore {
    override fun find(code: String): QualificationEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationEstimate) {
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

    override fun list(): MutableList<QualificationEstimate> {
        val out = mutableListOf<QualificationEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationEstimateStore(): QualificationEstimateStore {
    return MemoryQualificationEstimateStore(
        mutableMapOf<String, QualificationEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationEstimateStore(
    var inner: QualificationEstimateStore,
    var cache: TtlCache<QualificationEstimate>,
    var hits: Int,
    var misses: Int,
) : QualificationEstimateStore {
    override fun find(code: String): QualificationEstimate? {
        val hit = (cache.get(code) as QualificationEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationEstimate> = inner.list()
}

fun newCachedQualificationEstimateStore(inner: QualificationEstimateStore, ttl: Int, clock: Clock): QualificationEstimateStore {
    return CachedQualificationEstimateStore(
        inner = inner,
        cache = newTtlCache<QualificationEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification estimates. Empty fields match all. */
data class QualificationEstimateQuery(
    var locale: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationEstimate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationEstimate>): MutableList<QualificationEstimate> {
        val matched = filter(items) { candidate: QualificationEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationEstimates(store: QualificationEstimateStore, query: QualificationEstimateQuery): MutableList<QualificationEstimate> {
    return query.apply(store.list())
}
