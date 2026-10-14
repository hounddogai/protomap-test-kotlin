package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered roster records by code. */
interface TieredRosterRecordStore {
    fun find(code: String): TieredRosterRecord?
    fun save(item: TieredRosterRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRosterRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRosterRecordStore(
    var items: MutableMap<String, TieredRosterRecord>,
    var order: MutableList<String>,
) : TieredRosterRecordStore {
    override fun find(code: String): TieredRosterRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRosterRecord) {
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

    override fun list(): MutableList<TieredRosterRecord> {
        val out = mutableListOf<TieredRosterRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRosterRecordStore(): TieredRosterRecordStore {
    return MemoryTieredRosterRecordStore(mutableMapOf<String, TieredRosterRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRosterRecordStore(
    var inner: TieredRosterRecordStore,
    var cache: TtlCache<TieredRosterRecord>,
    var hits: Int,
    var misses: Int,
) : TieredRosterRecordStore {
    override fun find(code: String): TieredRosterRecord? {
        val hit = (cache.get(code) as TieredRosterRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRosterRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRosterRecord> = inner.list()
}

fun newCachedTieredRosterRecordStore(inner: TieredRosterRecordStore, ttl: Int, clock: Clock): TieredRosterRecordStore {
    return CachedTieredRosterRecordStore(
        inner = inner,
        cache = newTtlCache<TieredRosterRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered roster records. Empty fields match all. */
data class TieredRosterRecordQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRosterRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TieredRosterRecord>): MutableList<TieredRosterRecord> {
        val matched = filter(items) { candidate: TieredRosterRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRosterRecords(store: TieredRosterRecordStore, query: TieredRosterRecordQuery): MutableList<TieredRosterRecord> {
    return query.apply(store.list())
}
