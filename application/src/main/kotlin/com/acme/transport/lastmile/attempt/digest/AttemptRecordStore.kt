package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt records by code. */
interface AttemptRecordStore {
    fun find(code: String): AttemptRecord?
    fun save(item: AttemptRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptRecordStore(
    var items: MutableMap<String, AttemptRecord>,
    var order: MutableList<String>,
) : AttemptRecordStore {
    override fun find(code: String): AttemptRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptRecord) {
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

    override fun list(): MutableList<AttemptRecord> {
        val out = mutableListOf<AttemptRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptRecordStore(): AttemptRecordStore {
    return MemoryAttemptRecordStore(mutableMapOf<String, AttemptRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptRecordStore(
    var inner: AttemptRecordStore,
    var cache: TtlCache<AttemptRecord>,
    var hits: Int,
    var misses: Int,
) : AttemptRecordStore {
    override fun find(code: String): AttemptRecord? {
        val hit = (cache.get(code) as AttemptRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptRecord> = inner.list()
}

fun newCachedAttemptRecordStore(inner: AttemptRecordStore, ttl: Int, clock: Clock): AttemptRecordStore {
    return CachedAttemptRecordStore(
        inner = inner,
        cache = newTtlCache<AttemptRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt records. Empty fields match all. */
data class AttemptRecordQuery(
    var source: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptRecord): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptRecord>): MutableList<AttemptRecord> {
        val matched = filter(items) { candidate: AttemptRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptRecords(store: AttemptRecordStore, query: AttemptRecordQuery): MutableList<AttemptRecord> {
    return query.apply(store.list())
}
