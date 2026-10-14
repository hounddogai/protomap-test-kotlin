package com.acme.transport.lastmile.parcel.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched parcel templates by code. */
interface BatchedParcelTemplateStore {
    fun find(code: String): BatchedParcelTemplate?
    fun save(item: BatchedParcelTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedParcelTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedParcelTemplateStore(
    var items: MutableMap<String, BatchedParcelTemplate>,
    var order: MutableList<String>,
) : BatchedParcelTemplateStore {
    override fun find(code: String): BatchedParcelTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedParcelTemplate) {
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

    override fun list(): MutableList<BatchedParcelTemplate> {
        val out = mutableListOf<BatchedParcelTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedParcelTemplateStore(): BatchedParcelTemplateStore {
    return MemoryBatchedParcelTemplateStore(
        mutableMapOf<String, BatchedParcelTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedParcelTemplateStore(
    var inner: BatchedParcelTemplateStore,
    var cache: TtlCache<BatchedParcelTemplate>,
    var hits: Int,
    var misses: Int,
) : BatchedParcelTemplateStore {
    override fun find(code: String): BatchedParcelTemplate? {
        val hit = (cache.get(code) as BatchedParcelTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedParcelTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedParcelTemplate> = inner.list()
}

fun newCachedBatchedParcelTemplateStore(inner: BatchedParcelTemplateStore, ttl: Int, clock: Clock): BatchedParcelTemplateStore {
    return CachedBatchedParcelTemplateStore(
        inner = inner,
        cache = newTtlCache<BatchedParcelTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched parcel templates. Empty fields match all. */
data class BatchedParcelTemplateQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedParcelTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedParcelTemplate>): MutableList<BatchedParcelTemplate> {
        val matched = filter(items) { candidate: BatchedParcelTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedParcelTemplates(store: BatchedParcelTemplateStore, query: BatchedParcelTemplateQuery): MutableList<BatchedParcelTemplate> {
    return query.apply(store.list())
}
