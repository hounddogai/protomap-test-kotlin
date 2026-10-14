package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global trailer records by code. */
interface GlobalTrailerRecordStore {
    fun find(code: String): GlobalTrailerRecord?
    fun save(item: GlobalTrailerRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTrailerRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTrailerRecordStore(
    var items: MutableMap<String, GlobalTrailerRecord>,
    var order: MutableList<String>,
) : GlobalTrailerRecordStore {
    override fun find(code: String): GlobalTrailerRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTrailerRecord) {
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

    override fun list(): MutableList<GlobalTrailerRecord> {
        val out = mutableListOf<GlobalTrailerRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTrailerRecordStore(): GlobalTrailerRecordStore {
    return MemoryGlobalTrailerRecordStore(mutableMapOf<String, GlobalTrailerRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTrailerRecordStore(
    var inner: GlobalTrailerRecordStore,
    var cache: TtlCache<GlobalTrailerRecord>,
    var hits: Int,
    var misses: Int,
) : GlobalTrailerRecordStore {
    override fun find(code: String): GlobalTrailerRecord? {
        val hit = (cache.get(code) as GlobalTrailerRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTrailerRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTrailerRecord> = inner.list()
}

fun newCachedGlobalTrailerRecordStore(inner: GlobalTrailerRecordStore, ttl: Int, clock: Clock): GlobalTrailerRecordStore {
    return CachedGlobalTrailerRecordStore(
        inner = inner,
        cache = newTtlCache<GlobalTrailerRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global trailer records. Empty fields match all. */
data class GlobalTrailerRecordQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTrailerRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<GlobalTrailerRecord>): MutableList<GlobalTrailerRecord> {
        val matched = filter(items) { candidate: GlobalTrailerRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTrailerRecords(store: GlobalTrailerRecordStore, query: GlobalTrailerRecordQuery): MutableList<GlobalTrailerRecord> {
    return query.apply(store.list())
}
