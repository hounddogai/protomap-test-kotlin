package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss batches by code. */
interface NearMissBatchStore {
    fun find(code: String): NearMissBatch?
    fun save(item: NearMissBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissBatchStore(
    var items: MutableMap<String, NearMissBatch>,
    var order: MutableList<String>,
) : NearMissBatchStore {
    override fun find(code: String): NearMissBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissBatch) {
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

    override fun list(): MutableList<NearMissBatch> {
        val out = mutableListOf<NearMissBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissBatchStore(): NearMissBatchStore {
    return MemoryNearMissBatchStore(mutableMapOf<String, NearMissBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissBatchStore(
    var inner: NearMissBatchStore,
    var cache: TtlCache<NearMissBatch>,
    var hits: Int,
    var misses: Int,
) : NearMissBatchStore {
    override fun find(code: String): NearMissBatch? {
        val hit = (cache.get(code) as NearMissBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissBatch> = inner.list()
}

fun newCachedNearMissBatchStore(inner: NearMissBatchStore, ttl: Int, clock: Clock): NearMissBatchStore {
    return CachedNearMissBatchStore(
        inner = inner,
        cache = newTtlCache<NearMissBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss batches. Empty fields match all. */
data class NearMissBatchQuery(
    var lane: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissBatch): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<NearMissBatch>): MutableList<NearMissBatch> {
        val matched = filter(items) { candidate: NearMissBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissBatches(store: NearMissBatchStore, query: NearMissBatchQuery): MutableList<NearMissBatch> {
    return query.apply(store.list())
}
