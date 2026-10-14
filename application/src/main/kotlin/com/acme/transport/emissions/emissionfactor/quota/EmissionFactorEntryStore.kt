package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor entries by code. */
interface EmissionFactorEntryStore {
    fun find(code: String): EmissionFactorEntry?
    fun save(item: EmissionFactorEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorEntryStore(
    var items: MutableMap<String, EmissionFactorEntry>,
    var order: MutableList<String>,
) : EmissionFactorEntryStore {
    override fun find(code: String): EmissionFactorEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorEntry) {
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

    override fun list(): MutableList<EmissionFactorEntry> {
        val out = mutableListOf<EmissionFactorEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorEntryStore(): EmissionFactorEntryStore {
    return MemoryEmissionFactorEntryStore(mutableMapOf<String, EmissionFactorEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorEntryStore(
    var inner: EmissionFactorEntryStore,
    var cache: TtlCache<EmissionFactorEntry>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorEntryStore {
    override fun find(code: String): EmissionFactorEntry? {
        val hit = (cache.get(code) as EmissionFactorEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorEntry> = inner.list()
}

fun newCachedEmissionFactorEntryStore(inner: EmissionFactorEntryStore, ttl: Int, clock: Clock): EmissionFactorEntryStore {
    return CachedEmissionFactorEntryStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor entries. Empty fields match all. */
data class EmissionFactorEntryQuery(
    var source: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorEntry): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorEntry>): MutableList<EmissionFactorEntry> {
        val matched = filter(items) { candidate: EmissionFactorEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorEntries(store: EmissionFactorEntryStore, query: EmissionFactorEntryQuery): MutableList<EmissionFactorEntry> {
    return query.apply(store.list())
}
