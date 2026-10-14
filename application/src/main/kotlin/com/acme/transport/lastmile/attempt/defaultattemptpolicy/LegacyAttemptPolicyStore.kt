package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy attempt policies by code. */
interface LegacyAttemptPolicyStore {
    fun find(code: String): LegacyAttemptPolicy?
    fun save(item: LegacyAttemptPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAttemptPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAttemptPolicyStore(
    var items: MutableMap<String, LegacyAttemptPolicy>,
    var order: MutableList<String>,
) : LegacyAttemptPolicyStore {
    override fun find(code: String): LegacyAttemptPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAttemptPolicy) {
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

    override fun list(): MutableList<LegacyAttemptPolicy> {
        val out = mutableListOf<LegacyAttemptPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAttemptPolicyStore(): LegacyAttemptPolicyStore {
    return MemoryLegacyAttemptPolicyStore(mutableMapOf<String, LegacyAttemptPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAttemptPolicyStore(
    var inner: LegacyAttemptPolicyStore,
    var cache: TtlCache<LegacyAttemptPolicy>,
    var hits: Int,
    var misses: Int,
) : LegacyAttemptPolicyStore {
    override fun find(code: String): LegacyAttemptPolicy? {
        val hit = (cache.get(code) as LegacyAttemptPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAttemptPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAttemptPolicy> = inner.list()
}

fun newCachedLegacyAttemptPolicyStore(inner: LegacyAttemptPolicyStore, ttl: Int, clock: Clock): LegacyAttemptPolicyStore {
    return CachedLegacyAttemptPolicyStore(
        inner = inner,
        cache = newTtlCache<LegacyAttemptPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy attempt policies. Empty fields match all. */
data class LegacyAttemptPolicyQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAttemptPolicy): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyAttemptPolicy>): MutableList<LegacyAttemptPolicy> {
        val matched = filter(items) { candidate: LegacyAttemptPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAttemptPolicies(store: LegacyAttemptPolicyStore, query: LegacyAttemptPolicyQuery): MutableList<LegacyAttemptPolicy> {
    return query.apply(store.list())
}
