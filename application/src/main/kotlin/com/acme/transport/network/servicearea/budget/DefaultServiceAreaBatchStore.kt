package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default service area batches by code. */
interface DefaultServiceAreaBatchStore {
    fun find(code: String): DefaultServiceAreaBatch?
    fun save(item: DefaultServiceAreaBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultServiceAreaBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultServiceAreaBatchStore(
    var items: MutableMap<String, DefaultServiceAreaBatch>,
    var order: MutableList<String>,
) : DefaultServiceAreaBatchStore {
    override fun find(code: String): DefaultServiceAreaBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultServiceAreaBatch) {
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

    override fun list(): MutableList<DefaultServiceAreaBatch> {
        val out = mutableListOf<DefaultServiceAreaBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultServiceAreaBatchStore(): DefaultServiceAreaBatchStore {
    return MemoryDefaultServiceAreaBatchStore(
        mutableMapOf<String, DefaultServiceAreaBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultServiceAreaBatchStore(
    var inner: DefaultServiceAreaBatchStore,
    var cache: TtlCache<DefaultServiceAreaBatch>,
    var hits: Int,
    var misses: Int,
) : DefaultServiceAreaBatchStore {
    override fun find(code: String): DefaultServiceAreaBatch? {
        val hit = (cache.get(code) as DefaultServiceAreaBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultServiceAreaBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultServiceAreaBatch> = inner.list()
}

fun newCachedDefaultServiceAreaBatchStore(inner: DefaultServiceAreaBatchStore, ttl: Int, clock: Clock): DefaultServiceAreaBatchStore {
    return CachedDefaultServiceAreaBatchStore(
        inner = inner,
        cache = newTtlCache<DefaultServiceAreaBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default service area batches. Empty fields match all. */
data class DefaultServiceAreaBatchQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultServiceAreaBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultServiceAreaBatch>): MutableList<DefaultServiceAreaBatch> {
        val matched = filter(items) { candidate: DefaultServiceAreaBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultServiceAreaBatches(store: DefaultServiceAreaBatchStore, query: DefaultServiceAreaBatchQuery): MutableList<DefaultServiceAreaBatch> {
    return query.apply(store.list())
}
