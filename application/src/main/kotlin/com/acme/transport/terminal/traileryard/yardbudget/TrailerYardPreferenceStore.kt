package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard preferences by code. */
interface TrailerYardPreferenceStore {
    fun find(code: String): TrailerYardPreference?
    fun save(item: TrailerYardPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardPreferenceStore(
    var items: MutableMap<String, TrailerYardPreference>,
    var order: MutableList<String>,
) : TrailerYardPreferenceStore {
    override fun find(code: String): TrailerYardPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardPreference) {
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

    override fun list(): MutableList<TrailerYardPreference> {
        val out = mutableListOf<TrailerYardPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardPreferenceStore(): TrailerYardPreferenceStore {
    return MemoryTrailerYardPreferenceStore(
        mutableMapOf<String, TrailerYardPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardPreferenceStore(
    var inner: TrailerYardPreferenceStore,
    var cache: TtlCache<TrailerYardPreference>,
    var hits: Int,
    var misses: Int,
) : TrailerYardPreferenceStore {
    override fun find(code: String): TrailerYardPreference? {
        val hit = (cache.get(code) as TrailerYardPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardPreference> = inner.list()
}

fun newCachedTrailerYardPreferenceStore(inner: TrailerYardPreferenceStore, ttl: Int, clock: Clock): TrailerYardPreferenceStore {
    return CachedTrailerYardPreferenceStore(
        inner = inner,
        cache = newTtlCache<TrailerYardPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard preferences. Empty fields match all. */
data class TrailerYardPreferenceQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardPreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardPreference>): MutableList<TrailerYardPreference> {
        val matched = filter(items) { candidate: TrailerYardPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardPreferences(store: TrailerYardPreferenceStore, query: TrailerYardPreferenceQuery): MutableList<TrailerYardPreference> {
    return query.apply(store.list())
}
