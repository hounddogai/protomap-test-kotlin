package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive drop records by code. */
interface AdaptiveDropRecordStore {
    fun find(code: String): AdaptiveDropRecord?
    fun save(item: AdaptiveDropRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDropRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDropRecordStore(
    var items: MutableMap<String, AdaptiveDropRecord>,
    var order: MutableList<String>,
) : AdaptiveDropRecordStore {
    override fun find(code: String): AdaptiveDropRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDropRecord) {
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

    override fun list(): MutableList<AdaptiveDropRecord> {
        val out = mutableListOf<AdaptiveDropRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDropRecordStore(): AdaptiveDropRecordStore {
    return MemoryAdaptiveDropRecordStore(mutableMapOf<String, AdaptiveDropRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDropRecordStore(
    var inner: AdaptiveDropRecordStore,
    var cache: TtlCache<AdaptiveDropRecord>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDropRecordStore {
    override fun find(code: String): AdaptiveDropRecord? {
        val hit = (cache.get(code) as AdaptiveDropRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDropRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDropRecord> = inner.list()
}

fun newCachedAdaptiveDropRecordStore(inner: AdaptiveDropRecordStore, ttl: Int, clock: Clock): AdaptiveDropRecordStore {
    return CachedAdaptiveDropRecordStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDropRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive drop records. Empty fields match all. */
data class AdaptiveDropRecordQuery(
    var country: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDropRecord): Boolean {
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

    fun apply(items: MutableList<AdaptiveDropRecord>): MutableList<AdaptiveDropRecord> {
        val matched = filter(items) { candidate: AdaptiveDropRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDropRecords(store: AdaptiveDropRecordStore, query: AdaptiveDropRecordQuery): MutableList<AdaptiveDropRecord> {
    return query.apply(store.list())
}
