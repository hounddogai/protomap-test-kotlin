package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient near miss batches by code. */
interface LenientNearMissBatchStore {
    fun find(code: String): LenientNearMissBatch?
    fun save(item: LenientNearMissBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientNearMissBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientNearMissBatchStore(
    var items: MutableMap<String, LenientNearMissBatch>,
    var order: MutableList<String>,
) : LenientNearMissBatchStore {
    override fun find(code: String): LenientNearMissBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientNearMissBatch) {
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

    override fun list(): MutableList<LenientNearMissBatch> {
        val out = mutableListOf<LenientNearMissBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientNearMissBatchStore(): LenientNearMissBatchStore {
    return MemoryLenientNearMissBatchStore(
        mutableMapOf<String, LenientNearMissBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientNearMissBatchStore(
    var inner: LenientNearMissBatchStore,
    var cache: TtlCache<LenientNearMissBatch>,
    var hits: Int,
    var misses: Int,
) : LenientNearMissBatchStore {
    override fun find(code: String): LenientNearMissBatch? {
        val hit = (cache.get(code) as LenientNearMissBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientNearMissBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientNearMissBatch> = inner.list()
}

fun newCachedLenientNearMissBatchStore(inner: LenientNearMissBatchStore, ttl: Int, clock: Clock): LenientNearMissBatchStore {
    return CachedLenientNearMissBatchStore(
        inner = inner,
        cache = newTtlCache<LenientNearMissBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient near miss batches. Empty fields match all. */
data class LenientNearMissBatchQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientNearMissBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientNearMissBatch>): MutableList<LenientNearMissBatch> {
        val matched = filter(items) { candidate: LenientNearMissBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientNearMissBatches(store: LenientNearMissBatchStore, query: LenientNearMissBatchQuery): MutableList<LenientNearMissBatch> {
    return query.apply(store.list())
}
