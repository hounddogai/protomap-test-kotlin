package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal parcel records by code. */
interface SeasonalParcelRecordStore {
    fun find(code: String): SeasonalParcelRecord?
    fun save(item: SeasonalParcelRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalParcelRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalParcelRecordStore(
    var items: MutableMap<String, SeasonalParcelRecord>,
    var order: MutableList<String>,
) : SeasonalParcelRecordStore {
    override fun find(code: String): SeasonalParcelRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalParcelRecord) {
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

    override fun list(): MutableList<SeasonalParcelRecord> {
        val out = mutableListOf<SeasonalParcelRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalParcelRecordStore(): SeasonalParcelRecordStore {
    return MemorySeasonalParcelRecordStore(
        mutableMapOf<String, SeasonalParcelRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalParcelRecordStore(
    var inner: SeasonalParcelRecordStore,
    var cache: TtlCache<SeasonalParcelRecord>,
    var hits: Int,
    var misses: Int,
) : SeasonalParcelRecordStore {
    override fun find(code: String): SeasonalParcelRecord? {
        val hit = (cache.get(code) as SeasonalParcelRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalParcelRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalParcelRecord> = inner.list()
}

fun newCachedSeasonalParcelRecordStore(inner: SeasonalParcelRecordStore, ttl: Int, clock: Clock): SeasonalParcelRecordStore {
    return CachedSeasonalParcelRecordStore(
        inner = inner,
        cache = newTtlCache<SeasonalParcelRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal parcel records. Empty fields match all. */
data class SeasonalParcelRecordQuery(
    var locale: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalParcelRecord): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalParcelRecord>): MutableList<SeasonalParcelRecord> {
        val matched = filter(items) { candidate: SeasonalParcelRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalParcelRecords(store: SeasonalParcelRecordStore, query: SeasonalParcelRecordQuery): MutableList<SeasonalParcelRecord> {
    return query.apply(store.list())
}
