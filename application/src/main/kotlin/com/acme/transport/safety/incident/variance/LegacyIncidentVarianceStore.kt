package com.acme.transport.safety.incident.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy incident variances by code. */
interface LegacyIncidentVarianceStore {
    fun find(code: String): LegacyIncidentVariance?
    fun save(item: LegacyIncidentVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyIncidentVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyIncidentVarianceStore(
    var items: MutableMap<String, LegacyIncidentVariance>,
    var order: MutableList<String>,
) : LegacyIncidentVarianceStore {
    override fun find(code: String): LegacyIncidentVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyIncidentVariance) {
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

    override fun list(): MutableList<LegacyIncidentVariance> {
        val out = mutableListOf<LegacyIncidentVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyIncidentVarianceStore(): LegacyIncidentVarianceStore {
    return MemoryLegacyIncidentVarianceStore(
        mutableMapOf<String, LegacyIncidentVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyIncidentVarianceStore(
    var inner: LegacyIncidentVarianceStore,
    var cache: TtlCache<LegacyIncidentVariance>,
    var hits: Int,
    var misses: Int,
) : LegacyIncidentVarianceStore {
    override fun find(code: String): LegacyIncidentVariance? {
        val hit = (cache.get(code) as LegacyIncidentVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyIncidentVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyIncidentVariance> = inner.list()
}

fun newCachedLegacyIncidentVarianceStore(inner: LegacyIncidentVarianceStore, ttl: Int, clock: Clock): LegacyIncidentVarianceStore {
    return CachedLegacyIncidentVarianceStore(
        inner = inner,
        cache = newTtlCache<LegacyIncidentVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy incident variances. Empty fields match all. */
data class LegacyIncidentVarianceQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyIncidentVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyIncidentVariance>): MutableList<LegacyIncidentVariance> {
        val matched = filter(items) { candidate: LegacyIncidentVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyIncidentVariances(store: LegacyIncidentVarianceStore, query: LegacyIncidentVarianceQuery): MutableList<LegacyIncidentVariance> {
    return query.apply(store.list())
}
