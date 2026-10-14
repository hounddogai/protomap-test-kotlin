package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling attempt records by code. */
interface RollingAttemptRecordStore {
    fun find(code: String): RollingAttemptRecord?
    fun save(item: RollingAttemptRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAttemptRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAttemptRecordStore(
    var items: MutableMap<String, RollingAttemptRecord>,
    var order: MutableList<String>,
) : RollingAttemptRecordStore {
    override fun find(code: String): RollingAttemptRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAttemptRecord) {
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

    override fun list(): MutableList<RollingAttemptRecord> {
        val out = mutableListOf<RollingAttemptRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAttemptRecordStore(): RollingAttemptRecordStore {
    return MemoryRollingAttemptRecordStore(
        mutableMapOf<String, RollingAttemptRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAttemptRecordStore(
    var inner: RollingAttemptRecordStore,
    var cache: TtlCache<RollingAttemptRecord>,
    var hits: Int,
    var misses: Int,
) : RollingAttemptRecordStore {
    override fun find(code: String): RollingAttemptRecord? {
        val hit = (cache.get(code) as RollingAttemptRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAttemptRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAttemptRecord> = inner.list()
}

fun newCachedRollingAttemptRecordStore(inner: RollingAttemptRecordStore, ttl: Int, clock: Clock): RollingAttemptRecordStore {
    return CachedRollingAttemptRecordStore(
        inner = inner,
        cache = newTtlCache<RollingAttemptRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling attempt records. Empty fields match all. */
data class RollingAttemptRecordQuery(
    var depot: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAttemptRecord): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAttemptRecord>): MutableList<RollingAttemptRecord> {
        val matched = filter(items) { candidate: RollingAttemptRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAttemptRecords(store: RollingAttemptRecordStore, query: RollingAttemptRecordQuery): MutableList<RollingAttemptRecord> {
    return query.apply(store.list())
}
