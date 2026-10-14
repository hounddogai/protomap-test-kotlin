package com.acme.transport.customs.tariff.preference

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff preferences by code. */
interface TariffPreferenceStore {
    fun find(code: String): TariffPreference?
    fun save(item: TariffPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffPreferenceStore(
    var items: MutableMap<String, TariffPreference>,
    var order: MutableList<String>,
) : TariffPreferenceStore {
    override fun find(code: String): TariffPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffPreference) {
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

    override fun list(): MutableList<TariffPreference> {
        val out = mutableListOf<TariffPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffPreferenceStore(): TariffPreferenceStore {
    return MemoryTariffPreferenceStore(mutableMapOf<String, TariffPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffPreferenceStore(
    var inner: TariffPreferenceStore,
    var cache: TtlCache<TariffPreference>,
    var hits: Int,
    var misses: Int,
) : TariffPreferenceStore {
    override fun find(code: String): TariffPreference? {
        val hit = (cache.get(code) as TariffPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffPreference> = inner.list()
}

fun newCachedTariffPreferenceStore(inner: TariffPreferenceStore, ttl: Int, clock: Clock): TariffPreferenceStore {
    return CachedTariffPreferenceStore(
        inner = inner,
        cache = newTtlCache<TariffPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff preferences. Empty fields match all. */
data class TariffPreferenceQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffPreference): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffPreference>): MutableList<TariffPreference> {
        val matched = filter(items) { candidate: TariffPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffPreferences(store: TariffPreferenceStore, query: TariffPreferenceQuery): MutableList<TariffPreference> {
    return query.apply(store.list())
}
