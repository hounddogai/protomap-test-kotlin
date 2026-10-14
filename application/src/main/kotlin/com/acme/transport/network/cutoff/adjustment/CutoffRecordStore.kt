package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff records by code. */
interface CutoffRecordStore {
    fun find(code: String): CutoffRecord?
    fun save(item: CutoffRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffRecordStore(
    var items: MutableMap<String, CutoffRecord>,
    var order: MutableList<String>,
) : CutoffRecordStore {
    override fun find(code: String): CutoffRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffRecord) {
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

    override fun list(): MutableList<CutoffRecord> {
        val out = mutableListOf<CutoffRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffRecordStore(): CutoffRecordStore {
    return MemoryCutoffRecordStore(mutableMapOf<String, CutoffRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffRecordStore(
    var inner: CutoffRecordStore,
    var cache: TtlCache<CutoffRecord>,
    var hits: Int,
    var misses: Int,
) : CutoffRecordStore {
    override fun find(code: String): CutoffRecord? {
        val hit = (cache.get(code) as CutoffRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffRecord> = inner.list()
}

fun newCachedCutoffRecordStore(inner: CutoffRecordStore, ttl: Int, clock: Clock): CutoffRecordStore {
    return CachedCutoffRecordStore(
        inner = inner,
        cache = newTtlCache<CutoffRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff records. Empty fields match all. */
data class CutoffRecordQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffRecord>): MutableList<CutoffRecord> {
        val matched = filter(items) { candidate: CutoffRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffRecords(store: CutoffRecordStore, query: CutoffRecordQuery): MutableList<CutoffRecord> {
    return query.apply(store.list())
}
