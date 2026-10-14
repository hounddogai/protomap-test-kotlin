package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive emission factor entries by code. */
interface AdaptiveEmissionFactorEntryStore {
    fun find(code: String): AdaptiveEmissionFactorEntry?
    fun save(item: AdaptiveEmissionFactorEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveEmissionFactorEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveEmissionFactorEntryStore(
    var items: MutableMap<String, AdaptiveEmissionFactorEntry>,
    var order: MutableList<String>,
) : AdaptiveEmissionFactorEntryStore {
    override fun find(code: String): AdaptiveEmissionFactorEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveEmissionFactorEntry) {
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

    override fun list(): MutableList<AdaptiveEmissionFactorEntry> {
        val out = mutableListOf<AdaptiveEmissionFactorEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveEmissionFactorEntryStore(): AdaptiveEmissionFactorEntryStore {
    return MemoryAdaptiveEmissionFactorEntryStore(
        mutableMapOf<String, AdaptiveEmissionFactorEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveEmissionFactorEntryStore(
    var inner: AdaptiveEmissionFactorEntryStore,
    var cache: TtlCache<AdaptiveEmissionFactorEntry>,
    var hits: Int,
    var misses: Int,
) : AdaptiveEmissionFactorEntryStore {
    override fun find(code: String): AdaptiveEmissionFactorEntry? {
        val hit = (cache.get(code) as AdaptiveEmissionFactorEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveEmissionFactorEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveEmissionFactorEntry> = inner.list()
}

fun newCachedAdaptiveEmissionFactorEntryStore(inner: AdaptiveEmissionFactorEntryStore, ttl: Int, clock: Clock): AdaptiveEmissionFactorEntryStore {
    return CachedAdaptiveEmissionFactorEntryStore(
        inner = inner,
        cache = newTtlCache<AdaptiveEmissionFactorEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive emission factor entries. Empty fields match all. */
data class AdaptiveEmissionFactorEntryQuery(
    var locale: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveEmissionFactorEntry): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveEmissionFactorEntry>): MutableList<AdaptiveEmissionFactorEntry> {
        val matched = filter(items) { candidate: AdaptiveEmissionFactorEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveEmissionFactorEntries(store: AdaptiveEmissionFactorEntryStore, query: AdaptiveEmissionFactorEntryQuery): MutableList<AdaptiveEmissionFactorEntry> {
    return query.apply(store.list())
}
