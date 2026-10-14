package com.acme.transport.customs.tariff.policy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient tariff policies by code. */
interface LenientTariffPolicyStore {
    fun find(code: String): LenientTariffPolicy?
    fun save(item: LenientTariffPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientTariffPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientTariffPolicyStore(
    var items: MutableMap<String, LenientTariffPolicy>,
    var order: MutableList<String>,
) : LenientTariffPolicyStore {
    override fun find(code: String): LenientTariffPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientTariffPolicy) {
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

    override fun list(): MutableList<LenientTariffPolicy> {
        val out = mutableListOf<LenientTariffPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientTariffPolicyStore(): LenientTariffPolicyStore {
    return MemoryLenientTariffPolicyStore(mutableMapOf<String, LenientTariffPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientTariffPolicyStore(
    var inner: LenientTariffPolicyStore,
    var cache: TtlCache<LenientTariffPolicy>,
    var hits: Int,
    var misses: Int,
) : LenientTariffPolicyStore {
    override fun find(code: String): LenientTariffPolicy? {
        val hit = (cache.get(code) as LenientTariffPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientTariffPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientTariffPolicy> = inner.list()
}

fun newCachedLenientTariffPolicyStore(inner: LenientTariffPolicyStore, ttl: Int, clock: Clock): LenientTariffPolicyStore {
    return CachedLenientTariffPolicyStore(
        inner = inner,
        cache = newTtlCache<LenientTariffPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient tariff policies. Empty fields match all. */
data class LenientTariffPolicyQuery(
    var source: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientTariffPolicy): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientTariffPolicy>): MutableList<LenientTariffPolicy> {
        val matched = filter(items) { candidate: LenientTariffPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientTariffPolicies(store: LenientTariffPolicyStore, query: LenientTariffPolicyQuery): MutableList<LenientTariffPolicy> {
    return query.apply(store.list())
}
