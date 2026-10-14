package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary cross dock records by code. */
interface PrimaryCrossDockRecordStore {
    fun find(code: String): PrimaryCrossDockRecord?
    fun save(item: PrimaryCrossDockRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCrossDockRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCrossDockRecordStore(
    var items: MutableMap<String, PrimaryCrossDockRecord>,
    var order: MutableList<String>,
) : PrimaryCrossDockRecordStore {
    override fun find(code: String): PrimaryCrossDockRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCrossDockRecord) {
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

    override fun list(): MutableList<PrimaryCrossDockRecord> {
        val out = mutableListOf<PrimaryCrossDockRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCrossDockRecordStore(): PrimaryCrossDockRecordStore {
    return MemoryPrimaryCrossDockRecordStore(
        mutableMapOf<String, PrimaryCrossDockRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCrossDockRecordStore(
    var inner: PrimaryCrossDockRecordStore,
    var cache: TtlCache<PrimaryCrossDockRecord>,
    var hits: Int,
    var misses: Int,
) : PrimaryCrossDockRecordStore {
    override fun find(code: String): PrimaryCrossDockRecord? {
        val hit = (cache.get(code) as PrimaryCrossDockRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCrossDockRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCrossDockRecord> = inner.list()
}

fun newCachedPrimaryCrossDockRecordStore(inner: PrimaryCrossDockRecordStore, ttl: Int, clock: Clock): PrimaryCrossDockRecordStore {
    return CachedPrimaryCrossDockRecordStore(
        inner = inner,
        cache = newTtlCache<PrimaryCrossDockRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary cross dock records. Empty fields match all. */
data class PrimaryCrossDockRecordQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCrossDockRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCrossDockRecord>): MutableList<PrimaryCrossDockRecord> {
        val matched = filter(items) { candidate: PrimaryCrossDockRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCrossDockRecords(store: PrimaryCrossDockRecordStore, query: PrimaryCrossDockRecordQuery): MutableList<PrimaryCrossDockRecord> {
    return query.apply(store.list())
}
