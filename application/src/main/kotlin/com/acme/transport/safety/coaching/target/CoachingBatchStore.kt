package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching batches by code. */
interface CoachingBatchStore {
    fun find(code: String): CoachingBatch?
    fun save(item: CoachingBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingBatchStore(
    var items: MutableMap<String, CoachingBatch>,
    var order: MutableList<String>,
) : CoachingBatchStore {
    override fun find(code: String): CoachingBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingBatch) {
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

    override fun list(): MutableList<CoachingBatch> {
        val out = mutableListOf<CoachingBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingBatchStore(): CoachingBatchStore {
    return MemoryCoachingBatchStore(mutableMapOf<String, CoachingBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingBatchStore(
    var inner: CoachingBatchStore,
    var cache: TtlCache<CoachingBatch>,
    var hits: Int,
    var misses: Int,
) : CoachingBatchStore {
    override fun find(code: String): CoachingBatch? {
        val hit = (cache.get(code) as CoachingBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingBatch> = inner.list()
}

fun newCachedCoachingBatchStore(inner: CoachingBatchStore, ttl: Int, clock: Clock): CoachingBatchStore {
    return CachedCoachingBatchStore(
        inner = inner,
        cache = newTtlCache<CoachingBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching batches. Empty fields match all. */
data class CoachingBatchQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingBatch>): MutableList<CoachingBatch> {
        val matched = filter(items) { candidate: CoachingBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingBatches(store: CoachingBatchStore, query: CoachingBatchQuery): MutableList<CoachingBatch> {
    return query.apply(store.list())
}
