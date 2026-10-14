package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global near miss records by code. */
interface GlobalNearMissRecordStore {
    fun find(code: String): GlobalNearMissRecord?
    fun save(item: GlobalNearMissRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalNearMissRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalNearMissRecordStore(
    var items: MutableMap<String, GlobalNearMissRecord>,
    var order: MutableList<String>,
) : GlobalNearMissRecordStore {
    override fun find(code: String): GlobalNearMissRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalNearMissRecord) {
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

    override fun list(): MutableList<GlobalNearMissRecord> {
        val out = mutableListOf<GlobalNearMissRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalNearMissRecordStore(): GlobalNearMissRecordStore {
    return MemoryGlobalNearMissRecordStore(
        mutableMapOf<String, GlobalNearMissRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalNearMissRecordStore(
    var inner: GlobalNearMissRecordStore,
    var cache: TtlCache<GlobalNearMissRecord>,
    var hits: Int,
    var misses: Int,
) : GlobalNearMissRecordStore {
    override fun find(code: String): GlobalNearMissRecord? {
        val hit = (cache.get(code) as GlobalNearMissRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalNearMissRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalNearMissRecord> = inner.list()
}

fun newCachedGlobalNearMissRecordStore(inner: GlobalNearMissRecordStore, ttl: Int, clock: Clock): GlobalNearMissRecordStore {
    return CachedGlobalNearMissRecordStore(
        inner = inner,
        cache = newTtlCache<GlobalNearMissRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global near miss records. Empty fields match all. */
data class GlobalNearMissRecordQuery(
    var region: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalNearMissRecord): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalNearMissRecord>): MutableList<GlobalNearMissRecord> {
        val matched = filter(items) { candidate: GlobalNearMissRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalNearMissRecords(store: GlobalNearMissRecordStore, query: GlobalNearMissRecordQuery): MutableList<GlobalNearMissRecord> {
    return query.apply(store.list())
}
