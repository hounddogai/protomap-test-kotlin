package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute records by code. */
interface DisputeRecordStore {
    fun find(code: String): DisputeRecord?
    fun save(item: DisputeRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeRecordStore(
    var items: MutableMap<String, DisputeRecord>,
    var order: MutableList<String>,
) : DisputeRecordStore {
    override fun find(code: String): DisputeRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeRecord) {
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

    override fun list(): MutableList<DisputeRecord> {
        val out = mutableListOf<DisputeRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeRecordStore(): DisputeRecordStore {
    return MemoryDisputeRecordStore(mutableMapOf<String, DisputeRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeRecordStore(
    var inner: DisputeRecordStore,
    var cache: TtlCache<DisputeRecord>,
    var hits: Int,
    var misses: Int,
) : DisputeRecordStore {
    override fun find(code: String): DisputeRecord? {
        val hit = (cache.get(code) as DisputeRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeRecord> = inner.list()
}

fun newCachedDisputeRecordStore(inner: DisputeRecordStore, ttl: Int, clock: Clock): DisputeRecordStore {
    return CachedDisputeRecordStore(
        inner = inner,
        cache = newTtlCache<DisputeRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute records. Empty fields match all. */
data class DisputeRecordQuery(
    var category: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeRecord): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<DisputeRecord>): MutableList<DisputeRecord> {
        val matched = filter(items) { candidate: DisputeRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeRecords(store: DisputeRecordStore, query: DisputeRecordQuery): MutableList<DisputeRecord> {
    return query.apply(store.list())
}
