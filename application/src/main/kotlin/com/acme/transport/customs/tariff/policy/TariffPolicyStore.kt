package com.acme.transport.customs.tariff.policy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff policies by code. */
interface TariffPolicyStore {
    fun find(code: String): TariffPolicy?
    fun save(item: TariffPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffPolicyStore(
    var items: MutableMap<String, TariffPolicy>,
    var order: MutableList<String>,
) : TariffPolicyStore {
    override fun find(code: String): TariffPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffPolicy) {
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

    override fun list(): MutableList<TariffPolicy> {
        val out = mutableListOf<TariffPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffPolicyStore(): TariffPolicyStore {
    return MemoryTariffPolicyStore(mutableMapOf<String, TariffPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffPolicyStore(
    var inner: TariffPolicyStore,
    var cache: TtlCache<TariffPolicy>,
    var hits: Int,
    var misses: Int,
) : TariffPolicyStore {
    override fun find(code: String): TariffPolicy? {
        val hit = (cache.get(code) as TariffPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffPolicy> = inner.list()
}

fun newCachedTariffPolicyStore(inner: TariffPolicyStore, ttl: Int, clock: Clock): TariffPolicyStore {
    return CachedTariffPolicyStore(
        inner = inner,
        cache = newTtlCache<TariffPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff policies. Empty fields match all. */
data class TariffPolicyQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TariffPolicy>): MutableList<TariffPolicy> {
        val matched = filter(items) { candidate: TariffPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffPolicies(store: TariffPolicyStore, query: TariffPolicyQuery): MutableList<TariffPolicy> {
    return query.apply(store.list())
}
