package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback ev charge records by code. */
interface FallbackEvChargeRecordStore {
    fun find(code: String): FallbackEvChargeRecord?
    fun save(item: FallbackEvChargeRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEvChargeRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEvChargeRecordStore(
    var items: MutableMap<String, FallbackEvChargeRecord>,
    var order: MutableList<String>,
) : FallbackEvChargeRecordStore {
    override fun find(code: String): FallbackEvChargeRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEvChargeRecord) {
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

    override fun list(): MutableList<FallbackEvChargeRecord> {
        val out = mutableListOf<FallbackEvChargeRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEvChargeRecordStore(): FallbackEvChargeRecordStore {
    return MemoryFallbackEvChargeRecordStore(
        mutableMapOf<String, FallbackEvChargeRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEvChargeRecordStore(
    var inner: FallbackEvChargeRecordStore,
    var cache: TtlCache<FallbackEvChargeRecord>,
    var hits: Int,
    var misses: Int,
) : FallbackEvChargeRecordStore {
    override fun find(code: String): FallbackEvChargeRecord? {
        val hit = (cache.get(code) as FallbackEvChargeRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEvChargeRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEvChargeRecord> = inner.list()
}

fun newCachedFallbackEvChargeRecordStore(inner: FallbackEvChargeRecordStore, ttl: Int, clock: Clock): FallbackEvChargeRecordStore {
    return CachedFallbackEvChargeRecordStore(
        inner = inner,
        cache = newTtlCache<FallbackEvChargeRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback ev charge records. Empty fields match all. */
data class FallbackEvChargeRecordQuery(
    var channel: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEvChargeRecord): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<FallbackEvChargeRecord>): MutableList<FallbackEvChargeRecord> {
        val matched = filter(items) { candidate: FallbackEvChargeRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEvChargeRecords(store: FallbackEvChargeRecordStore, query: FallbackEvChargeRecordQuery): MutableList<FallbackEvChargeRecord> {
    return query.apply(store.list())
}
