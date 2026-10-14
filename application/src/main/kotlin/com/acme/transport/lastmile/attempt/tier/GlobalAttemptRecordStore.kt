package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global attempt records by code. */
interface GlobalAttemptRecordStore {
    fun find(code: String): GlobalAttemptRecord?
    fun save(item: GlobalAttemptRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAttemptRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAttemptRecordStore(
    var items: MutableMap<String, GlobalAttemptRecord>,
    var order: MutableList<String>,
) : GlobalAttemptRecordStore {
    override fun find(code: String): GlobalAttemptRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAttemptRecord) {
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

    override fun list(): MutableList<GlobalAttemptRecord> {
        val out = mutableListOf<GlobalAttemptRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAttemptRecordStore(): GlobalAttemptRecordStore {
    return MemoryGlobalAttemptRecordStore(mutableMapOf<String, GlobalAttemptRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAttemptRecordStore(
    var inner: GlobalAttemptRecordStore,
    var cache: TtlCache<GlobalAttemptRecord>,
    var hits: Int,
    var misses: Int,
) : GlobalAttemptRecordStore {
    override fun find(code: String): GlobalAttemptRecord? {
        val hit = (cache.get(code) as GlobalAttemptRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAttemptRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAttemptRecord> = inner.list()
}

fun newCachedGlobalAttemptRecordStore(inner: GlobalAttemptRecordStore, ttl: Int, clock: Clock): GlobalAttemptRecordStore {
    return CachedGlobalAttemptRecordStore(
        inner = inner,
        cache = newTtlCache<GlobalAttemptRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global attempt records. Empty fields match all. */
data class GlobalAttemptRecordQuery(
    var source: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAttemptRecord): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<GlobalAttemptRecord>): MutableList<GlobalAttemptRecord> {
        val matched = filter(items) { candidate: GlobalAttemptRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAttemptRecords(store: GlobalAttemptRecordStore, query: GlobalAttemptRecordQuery): MutableList<GlobalAttemptRecord> {
    return query.apply(store.list())
}
