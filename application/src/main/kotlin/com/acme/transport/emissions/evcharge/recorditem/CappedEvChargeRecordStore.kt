package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped ev charge records by code. */
interface CappedEvChargeRecordStore {
    fun find(code: String): CappedEvChargeRecord?
    fun save(item: CappedEvChargeRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedEvChargeRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedEvChargeRecordStore(
    var items: MutableMap<String, CappedEvChargeRecord>,
    var order: MutableList<String>,
) : CappedEvChargeRecordStore {
    override fun find(code: String): CappedEvChargeRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedEvChargeRecord) {
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

    override fun list(): MutableList<CappedEvChargeRecord> {
        val out = mutableListOf<CappedEvChargeRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedEvChargeRecordStore(): CappedEvChargeRecordStore {
    return MemoryCappedEvChargeRecordStore(
        mutableMapOf<String, CappedEvChargeRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedEvChargeRecordStore(
    var inner: CappedEvChargeRecordStore,
    var cache: TtlCache<CappedEvChargeRecord>,
    var hits: Int,
    var misses: Int,
) : CappedEvChargeRecordStore {
    override fun find(code: String): CappedEvChargeRecord? {
        val hit = (cache.get(code) as CappedEvChargeRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedEvChargeRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedEvChargeRecord> = inner.list()
}

fun newCachedCappedEvChargeRecordStore(inner: CappedEvChargeRecordStore, ttl: Int, clock: Clock): CappedEvChargeRecordStore {
    return CachedCappedEvChargeRecordStore(
        inner = inner,
        cache = newTtlCache<CappedEvChargeRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped ev charge records. Empty fields match all. */
data class CappedEvChargeRecordQuery(
    var lane: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedEvChargeRecord): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedEvChargeRecord>): MutableList<CappedEvChargeRecord> {
        val matched = filter(items) { candidate: CappedEvChargeRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedEvChargeRecords(store: CappedEvChargeRecordStore, query: CappedEvChargeRecordQuery): MutableList<CappedEvChargeRecord> {
    return query.apply(store.list())
}
