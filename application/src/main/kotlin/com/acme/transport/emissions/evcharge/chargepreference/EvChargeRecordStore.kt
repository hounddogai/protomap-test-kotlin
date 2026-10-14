package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge records by code. */
interface EvChargeRecordStore {
    fun find(code: String): EvChargeRecord?
    fun save(item: EvChargeRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeRecordStore(
    var items: MutableMap<String, EvChargeRecord>,
    var order: MutableList<String>,
) : EvChargeRecordStore {
    override fun find(code: String): EvChargeRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeRecord) {
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

    override fun list(): MutableList<EvChargeRecord> {
        val out = mutableListOf<EvChargeRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeRecordStore(): EvChargeRecordStore {
    return MemoryEvChargeRecordStore(mutableMapOf<String, EvChargeRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeRecordStore(
    var inner: EvChargeRecordStore,
    var cache: TtlCache<EvChargeRecord>,
    var hits: Int,
    var misses: Int,
) : EvChargeRecordStore {
    override fun find(code: String): EvChargeRecord? {
        val hit = (cache.get(code) as EvChargeRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeRecord> = inner.list()
}

fun newCachedEvChargeRecordStore(inner: EvChargeRecordStore, ttl: Int, clock: Clock): EvChargeRecordStore {
    return CachedEvChargeRecordStore(
        inner = inner,
        cache = newTtlCache<EvChargeRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge records. Empty fields match all. */
data class EvChargeRecordQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeRecord): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeRecord>): MutableList<EvChargeRecord> {
        val matched = filter(items) { candidate: EvChargeRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeRecords(store: EvChargeRecordStore, query: EvChargeRecordQuery): MutableList<EvChargeRecord> {
    return query.apply(store.list())
}
