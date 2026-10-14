package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel records by code. */
interface ParcelRecordStore {
    fun find(code: String): ParcelRecord?
    fun save(item: ParcelRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelRecordStore(
    var items: MutableMap<String, ParcelRecord>,
    var order: MutableList<String>,
) : ParcelRecordStore {
    override fun find(code: String): ParcelRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelRecord) {
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

    override fun list(): MutableList<ParcelRecord> {
        val out = mutableListOf<ParcelRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelRecordStore(): ParcelRecordStore {
    return MemoryParcelRecordStore(mutableMapOf<String, ParcelRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelRecordStore(
    var inner: ParcelRecordStore,
    var cache: TtlCache<ParcelRecord>,
    var hits: Int,
    var misses: Int,
) : ParcelRecordStore {
    override fun find(code: String): ParcelRecord? {
        val hit = (cache.get(code) as ParcelRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelRecord> = inner.list()
}

fun newCachedParcelRecordStore(inner: ParcelRecordStore, ttl: Int, clock: Clock): ParcelRecordStore {
    return CachedParcelRecordStore(
        inner = inner,
        cache = newTtlCache<ParcelRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel records. Empty fields match all. */
data class ParcelRecordQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelRecord>): MutableList<ParcelRecord> {
        val matched = filter(items) { candidate: ParcelRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelRecords(store: ParcelRecordStore, query: ParcelRecordQuery): MutableList<ParcelRecord> {
    return query.apply(store.list())
}
