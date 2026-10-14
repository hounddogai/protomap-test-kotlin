package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling relay records by code. */
interface RollingRelayRecordStore {
    fun find(code: String): RollingRelayRecord?
    fun save(item: RollingRelayRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRelayRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRelayRecordStore(
    var items: MutableMap<String, RollingRelayRecord>,
    var order: MutableList<String>,
) : RollingRelayRecordStore {
    override fun find(code: String): RollingRelayRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRelayRecord) {
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

    override fun list(): MutableList<RollingRelayRecord> {
        val out = mutableListOf<RollingRelayRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRelayRecordStore(): RollingRelayRecordStore {
    return MemoryRollingRelayRecordStore(mutableMapOf<String, RollingRelayRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRelayRecordStore(
    var inner: RollingRelayRecordStore,
    var cache: TtlCache<RollingRelayRecord>,
    var hits: Int,
    var misses: Int,
) : RollingRelayRecordStore {
    override fun find(code: String): RollingRelayRecord? {
        val hit = (cache.get(code) as RollingRelayRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRelayRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRelayRecord> = inner.list()
}

fun newCachedRollingRelayRecordStore(inner: RollingRelayRecordStore, ttl: Int, clock: Clock): RollingRelayRecordStore {
    return CachedRollingRelayRecordStore(
        inner = inner,
        cache = newTtlCache<RollingRelayRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling relay records. Empty fields match all. */
data class RollingRelayRecordQuery(
    var region: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRelayRecord): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRelayRecord>): MutableList<RollingRelayRecord> {
        val matched = filter(items) { candidate: RollingRelayRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRelayRecords(store: RollingRelayRecordStore, query: RollingRelayRecordQuery): MutableList<RollingRelayRecord> {
    return query.apply(store.list())
}
