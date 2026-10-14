package com.acme.transport.network.hub.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy hub records by code. */
interface LegacyHubRecordStore {
    fun find(code: String): LegacyHubRecord?
    fun save(item: LegacyHubRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyHubRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyHubRecordStore(
    var items: MutableMap<String, LegacyHubRecord>,
    var order: MutableList<String>,
) : LegacyHubRecordStore {
    override fun find(code: String): LegacyHubRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyHubRecord) {
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

    override fun list(): MutableList<LegacyHubRecord> {
        val out = mutableListOf<LegacyHubRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyHubRecordStore(): LegacyHubRecordStore {
    return MemoryLegacyHubRecordStore(mutableMapOf<String, LegacyHubRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyHubRecordStore(
    var inner: LegacyHubRecordStore,
    var cache: TtlCache<LegacyHubRecord>,
    var hits: Int,
    var misses: Int,
) : LegacyHubRecordStore {
    override fun find(code: String): LegacyHubRecord? {
        val hit = (cache.get(code) as LegacyHubRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyHubRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyHubRecord> = inner.list()
}

fun newCachedLegacyHubRecordStore(inner: LegacyHubRecordStore, ttl: Int, clock: Clock): LegacyHubRecordStore {
    return CachedLegacyHubRecordStore(
        inner = inner,
        cache = newTtlCache<LegacyHubRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy hub records. Empty fields match all. */
data class LegacyHubRecordQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyHubRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyHubRecord>): MutableList<LegacyHubRecord> {
        val matched = filter(items) { candidate: LegacyHubRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyHubRecords(store: LegacyHubRecordStore, query: LegacyHubRecordQuery): MutableList<LegacyHubRecord> {
    return query.apply(store.list())
}
