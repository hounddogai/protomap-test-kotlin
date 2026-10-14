package com.acme.transport.customs.tariff.cycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered tariff bundles by code. */
interface TieredTariffBundleStore {
    fun find(code: String): TieredTariffBundle?
    fun save(item: TieredTariffBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTariffBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTariffBundleStore(
    var items: MutableMap<String, TieredTariffBundle>,
    var order: MutableList<String>,
) : TieredTariffBundleStore {
    override fun find(code: String): TieredTariffBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTariffBundle) {
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

    override fun list(): MutableList<TieredTariffBundle> {
        val out = mutableListOf<TieredTariffBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTariffBundleStore(): TieredTariffBundleStore {
    return MemoryTieredTariffBundleStore(mutableMapOf<String, TieredTariffBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTariffBundleStore(
    var inner: TieredTariffBundleStore,
    var cache: TtlCache<TieredTariffBundle>,
    var hits: Int,
    var misses: Int,
) : TieredTariffBundleStore {
    override fun find(code: String): TieredTariffBundle? {
        val hit = (cache.get(code) as TieredTariffBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTariffBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTariffBundle> = inner.list()
}

fun newCachedTieredTariffBundleStore(inner: TieredTariffBundleStore, ttl: Int, clock: Clock): TieredTariffBundleStore {
    return CachedTieredTariffBundleStore(
        inner = inner,
        cache = newTtlCache<TieredTariffBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered tariff bundles. Empty fields match all. */
data class TieredTariffBundleQuery(
    var country: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTariffBundle): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<TieredTariffBundle>): MutableList<TieredTariffBundle> {
        val matched = filter(items) { candidate: TieredTariffBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTariffBundles(store: TieredTariffBundleStore, query: TieredTariffBundleQuery): MutableList<TieredTariffBundle> {
    return query.apply(store.list())
}
