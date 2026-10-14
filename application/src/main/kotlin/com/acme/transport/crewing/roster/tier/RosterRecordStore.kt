package com.acme.transport.crewing.roster.tier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster records by code. */
interface RosterRecordStore {
    fun find(code: String): RosterRecord?
    fun save(item: RosterRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterRecordStore(
    var items: MutableMap<String, RosterRecord>,
    var order: MutableList<String>,
) : RosterRecordStore {
    override fun find(code: String): RosterRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterRecord) {
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

    override fun list(): MutableList<RosterRecord> {
        val out = mutableListOf<RosterRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterRecordStore(): RosterRecordStore {
    return MemoryRosterRecordStore(mutableMapOf<String, RosterRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterRecordStore(
    var inner: RosterRecordStore,
    var cache: TtlCache<RosterRecord>,
    var hits: Int,
    var misses: Int,
) : RosterRecordStore {
    override fun find(code: String): RosterRecord? {
        val hit = (cache.get(code) as RosterRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterRecord> = inner.list()
}

fun newCachedRosterRecordStore(inner: RosterRecordStore, ttl: Int, clock: Clock): RosterRecordStore {
    return CachedRosterRecordStore(
        inner = inner,
        cache = newTtlCache<RosterRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster records. Empty fields match all. */
data class RosterRecordQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterRecord>): MutableList<RosterRecord> {
        val matched = filter(items) { candidate: RosterRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterRecords(store: RosterRecordStore, query: RosterRecordQuery): MutableList<RosterRecord> {
    return query.apply(store.list())
}
