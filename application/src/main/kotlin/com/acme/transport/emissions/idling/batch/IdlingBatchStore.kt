package com.acme.transport.emissions.idling.batch

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling batches by code. */
interface IdlingBatchStore {
    fun find(code: String): IdlingBatch?
    fun save(item: IdlingBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingBatchStore(
    var items: MutableMap<String, IdlingBatch>,
    var order: MutableList<String>,
) : IdlingBatchStore {
    override fun find(code: String): IdlingBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingBatch) {
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

    override fun list(): MutableList<IdlingBatch> {
        val out = mutableListOf<IdlingBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingBatchStore(): IdlingBatchStore {
    return MemoryIdlingBatchStore(mutableMapOf<String, IdlingBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingBatchStore(
    var inner: IdlingBatchStore,
    var cache: TtlCache<IdlingBatch>,
    var hits: Int,
    var misses: Int,
) : IdlingBatchStore {
    override fun find(code: String): IdlingBatch? {
        val hit = (cache.get(code) as IdlingBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingBatch> = inner.list()
}

fun newCachedIdlingBatchStore(inner: IdlingBatchStore, ttl: Int, clock: Clock): IdlingBatchStore {
    return CachedIdlingBatchStore(
        inner = inner,
        cache = newTtlCache<IdlingBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling batches. Empty fields match all. */
data class IdlingBatchQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingBatch>): MutableList<IdlingBatch> {
        val matched = filter(items) { candidate: IdlingBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingBatches(store: IdlingBatchStore, query: IdlingBatchQuery): MutableList<IdlingBatch> {
    return query.apply(store.list())
}
