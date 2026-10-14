package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender preferences by code. */
interface TenderPreferenceStore {
    fun find(code: String): TenderPreference?
    fun save(item: TenderPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderPreferenceStore(
    var items: MutableMap<String, TenderPreference>,
    var order: MutableList<String>,
) : TenderPreferenceStore {
    override fun find(code: String): TenderPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderPreference) {
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

    override fun list(): MutableList<TenderPreference> {
        val out = mutableListOf<TenderPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderPreferenceStore(): TenderPreferenceStore {
    return MemoryTenderPreferenceStore(mutableMapOf<String, TenderPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderPreferenceStore(
    var inner: TenderPreferenceStore,
    var cache: TtlCache<TenderPreference>,
    var hits: Int,
    var misses: Int,
) : TenderPreferenceStore {
    override fun find(code: String): TenderPreference? {
        val hit = (cache.get(code) as TenderPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderPreference> = inner.list()
}

fun newCachedTenderPreferenceStore(inner: TenderPreferenceStore, ttl: Int, clock: Clock): TenderPreferenceStore {
    return CachedTenderPreferenceStore(
        inner = inner,
        cache = newTtlCache<TenderPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender preferences. Empty fields match all. */
data class TenderPreferenceQuery(
    var category: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderPreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<TenderPreference>): MutableList<TenderPreference> {
        val matched = filter(items) { candidate: TenderPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderPreferences(store: TenderPreferenceStore, query: TenderPreferenceQuery): MutableList<TenderPreference> {
    return query.apply(store.list())
}
