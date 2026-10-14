package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff bundles by code. */
interface TariffBundleStore {
    fun find(code: String): TariffBundle?
    fun save(item: TariffBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffBundleStore(
    var items: MutableMap<String, TariffBundle>,
    var order: MutableList<String>,
) : TariffBundleStore {
    override fun find(code: String): TariffBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffBundle) {
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

    override fun list(): MutableList<TariffBundle> {
        val out = mutableListOf<TariffBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffBundleStore(): TariffBundleStore {
    return MemoryTariffBundleStore(mutableMapOf<String, TariffBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffBundleStore(
    var inner: TariffBundleStore,
    var cache: TtlCache<TariffBundle>,
    var hits: Int,
    var misses: Int,
) : TariffBundleStore {
    override fun find(code: String): TariffBundle? {
        val hit = (cache.get(code) as TariffBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffBundle> = inner.list()
}

fun newCachedTariffBundleStore(inner: TariffBundleStore, ttl: Int, clock: Clock): TariffBundleStore {
    return CachedTariffBundleStore(
        inner = inner,
        cache = newTtlCache<TariffBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff bundles. Empty fields match all. */
data class TariffBundleQuery(
    var category: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffBundle): Boolean {
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

    fun apply(items: MutableList<TariffBundle>): MutableList<TariffBundle> {
        val matched = filter(items) { candidate: TariffBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffBundles(store: TariffBundleStore, query: TariffBundleQuery): MutableList<TariffBundle> {
    return query.apply(store.list())
}
