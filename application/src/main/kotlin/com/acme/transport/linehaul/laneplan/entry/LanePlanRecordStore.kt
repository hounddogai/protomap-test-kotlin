package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan records by code. */
interface LanePlanRecordStore {
    fun find(code: String): LanePlanRecord?
    fun save(item: LanePlanRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanRecordStore(
    var items: MutableMap<String, LanePlanRecord>,
    var order: MutableList<String>,
) : LanePlanRecordStore {
    override fun find(code: String): LanePlanRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanRecord) {
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

    override fun list(): MutableList<LanePlanRecord> {
        val out = mutableListOf<LanePlanRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanRecordStore(): LanePlanRecordStore {
    return MemoryLanePlanRecordStore(mutableMapOf<String, LanePlanRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanRecordStore(
    var inner: LanePlanRecordStore,
    var cache: TtlCache<LanePlanRecord>,
    var hits: Int,
    var misses: Int,
) : LanePlanRecordStore {
    override fun find(code: String): LanePlanRecord? {
        val hit = (cache.get(code) as LanePlanRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanRecord> = inner.list()
}

fun newCachedLanePlanRecordStore(inner: LanePlanRecordStore, ttl: Int, clock: Clock): LanePlanRecordStore {
    return CachedLanePlanRecordStore(
        inner = inner,
        cache = newTtlCache<LanePlanRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan records. Empty fields match all. */
data class LanePlanRecordQuery(
    var depot: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanRecord): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<LanePlanRecord>): MutableList<LanePlanRecord> {
        val matched = filter(items) { candidate: LanePlanRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanRecords(store: LanePlanRecordStore, query: LanePlanRecordQuery): MutableList<LanePlanRecord> {
    return query.apply(store.list())
}
