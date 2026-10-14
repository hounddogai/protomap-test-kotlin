package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy qualification policies by code. */
interface LegacyQualificationPolicyStore {
    fun find(code: String): LegacyQualificationPolicy?
    fun save(item: LegacyQualificationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyQualificationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyQualificationPolicyStore(
    var items: MutableMap<String, LegacyQualificationPolicy>,
    var order: MutableList<String>,
) : LegacyQualificationPolicyStore {
    override fun find(code: String): LegacyQualificationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyQualificationPolicy) {
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

    override fun list(): MutableList<LegacyQualificationPolicy> {
        val out = mutableListOf<LegacyQualificationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyQualificationPolicyStore(): LegacyQualificationPolicyStore {
    return MemoryLegacyQualificationPolicyStore(
        mutableMapOf<String, LegacyQualificationPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyQualificationPolicyStore(
    var inner: LegacyQualificationPolicyStore,
    var cache: TtlCache<LegacyQualificationPolicy>,
    var hits: Int,
    var misses: Int,
) : LegacyQualificationPolicyStore {
    override fun find(code: String): LegacyQualificationPolicy? {
        val hit = (cache.get(code) as LegacyQualificationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyQualificationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyQualificationPolicy> = inner.list()
}

fun newCachedLegacyQualificationPolicyStore(inner: LegacyQualificationPolicyStore, ttl: Int, clock: Clock): LegacyQualificationPolicyStore {
    return CachedLegacyQualificationPolicyStore(
        inner = inner,
        cache = newTtlCache<LegacyQualificationPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy qualification policies. Empty fields match all. */
data class LegacyQualificationPolicyQuery(
    var category: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyQualificationPolicy): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyQualificationPolicy>): MutableList<LegacyQualificationPolicy> {
        val matched = filter(items) { candidate: LegacyQualificationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyQualificationPolicies(store: LegacyQualificationPolicyStore, query: LegacyQualificationPolicyQuery): MutableList<LegacyQualificationPolicy> {
    return query.apply(store.list())
}
