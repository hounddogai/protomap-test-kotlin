package com.acme.transport.linehaul.relay.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered relay records by code. */
interface TieredRelayRecordStore {
    fun find(code: String): TieredRelayRecord?
    fun save(item: TieredRelayRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRelayRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRelayRecordStore(
    var items: MutableMap<String, TieredRelayRecord>,
    var order: MutableList<String>,
) : TieredRelayRecordStore {
    override fun find(code: String): TieredRelayRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRelayRecord) {
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

    override fun list(): MutableList<TieredRelayRecord> {
        val out = mutableListOf<TieredRelayRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRelayRecordStore(): TieredRelayRecordStore {
    return MemoryTieredRelayRecordStore(mutableMapOf<String, TieredRelayRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRelayRecordStore(
    var inner: TieredRelayRecordStore,
    var cache: TtlCache<TieredRelayRecord>,
    var hits: Int,
    var misses: Int,
) : TieredRelayRecordStore {
    override fun find(code: String): TieredRelayRecord? {
        val hit = (cache.get(code) as TieredRelayRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRelayRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRelayRecord> = inner.list()
}

fun newCachedTieredRelayRecordStore(inner: TieredRelayRecordStore, ttl: Int, clock: Clock): TieredRelayRecordStore {
    return CachedTieredRelayRecordStore(
        inner = inner,
        cache = newTtlCache<TieredRelayRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered relay records. Empty fields match all. */
data class TieredRelayRecordQuery(
    var locale: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRelayRecord): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<TieredRelayRecord>): MutableList<TieredRelayRecord> {
        val matched = filter(items) { candidate: TieredRelayRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRelayRecords(store: TieredRelayRecordStore, query: TieredRelayRecordQuery): MutableList<TieredRelayRecord> {
    return query.apply(store.list())
}
