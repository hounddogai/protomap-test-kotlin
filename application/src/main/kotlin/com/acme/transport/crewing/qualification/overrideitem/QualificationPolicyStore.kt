package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification policies by code. */
interface QualificationPolicyStore {
    fun find(code: String): QualificationPolicy?
    fun save(item: QualificationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationPolicyStore(
    var items: MutableMap<String, QualificationPolicy>,
    var order: MutableList<String>,
) : QualificationPolicyStore {
    override fun find(code: String): QualificationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationPolicy) {
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

    override fun list(): MutableList<QualificationPolicy> {
        val out = mutableListOf<QualificationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationPolicyStore(): QualificationPolicyStore {
    return MemoryQualificationPolicyStore(mutableMapOf<String, QualificationPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationPolicyStore(
    var inner: QualificationPolicyStore,
    var cache: TtlCache<QualificationPolicy>,
    var hits: Int,
    var misses: Int,
) : QualificationPolicyStore {
    override fun find(code: String): QualificationPolicy? {
        val hit = (cache.get(code) as QualificationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationPolicy> = inner.list()
}

fun newCachedQualificationPolicyStore(inner: QualificationPolicyStore, ttl: Int, clock: Clock): QualificationPolicyStore {
    return CachedQualificationPolicyStore(
        inner = inner,
        cache = newTtlCache<QualificationPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification policies. Empty fields match all. */
data class QualificationPolicyQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationPolicy): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationPolicy>): MutableList<QualificationPolicy> {
        val matched = filter(items) { candidate: QualificationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationPolicies(store: QualificationPolicyStore, query: QualificationPolicyQuery): MutableList<QualificationPolicy> {
    return query.apply(store.list())
}
