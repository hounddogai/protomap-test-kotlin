package com.acme.transport.linehaul.laneplan.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled lane plan records by code. */
interface ScheduledLanePlanRecordStore {
    fun find(code: String): ScheduledLanePlanRecord?
    fun save(item: ScheduledLanePlanRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLanePlanRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLanePlanRecordStore(
    var items: MutableMap<String, ScheduledLanePlanRecord>,
    var order: MutableList<String>,
) : ScheduledLanePlanRecordStore {
    override fun find(code: String): ScheduledLanePlanRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLanePlanRecord) {
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

    override fun list(): MutableList<ScheduledLanePlanRecord> {
        val out = mutableListOf<ScheduledLanePlanRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLanePlanRecordStore(): ScheduledLanePlanRecordStore {
    return MemoryScheduledLanePlanRecordStore(
        mutableMapOf<String, ScheduledLanePlanRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLanePlanRecordStore(
    var inner: ScheduledLanePlanRecordStore,
    var cache: TtlCache<ScheduledLanePlanRecord>,
    var hits: Int,
    var misses: Int,
) : ScheduledLanePlanRecordStore {
    override fun find(code: String): ScheduledLanePlanRecord? {
        val hit = (cache.get(code) as ScheduledLanePlanRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLanePlanRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLanePlanRecord> = inner.list()
}

fun newCachedScheduledLanePlanRecordStore(inner: ScheduledLanePlanRecordStore, ttl: Int, clock: Clock): ScheduledLanePlanRecordStore {
    return CachedScheduledLanePlanRecordStore(
        inner = inner,
        cache = newTtlCache<ScheduledLanePlanRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled lane plan records. Empty fields match all. */
data class ScheduledLanePlanRecordQuery(
    var region: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLanePlanRecord): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLanePlanRecord>): MutableList<ScheduledLanePlanRecord> {
        val matched = filter(items) { candidate: ScheduledLanePlanRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLanePlanRecords(store: ScheduledLanePlanRecordStore, query: ScheduledLanePlanRecordQuery): MutableList<ScheduledLanePlanRecord> {
    return query.apply(store.list())
}
