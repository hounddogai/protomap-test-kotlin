package com.acme.transport.crewing.qualification.slot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched qualification templates by code. */
interface BatchedQualificationTemplateStore {
    fun find(code: String): BatchedQualificationTemplate?
    fun save(item: BatchedQualificationTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedQualificationTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedQualificationTemplateStore(
    var items: MutableMap<String, BatchedQualificationTemplate>,
    var order: MutableList<String>,
) : BatchedQualificationTemplateStore {
    override fun find(code: String): BatchedQualificationTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedQualificationTemplate) {
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

    override fun list(): MutableList<BatchedQualificationTemplate> {
        val out = mutableListOf<BatchedQualificationTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedQualificationTemplateStore(): BatchedQualificationTemplateStore {
    return MemoryBatchedQualificationTemplateStore(
        mutableMapOf<String, BatchedQualificationTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedQualificationTemplateStore(
    var inner: BatchedQualificationTemplateStore,
    var cache: TtlCache<BatchedQualificationTemplate>,
    var hits: Int,
    var misses: Int,
) : BatchedQualificationTemplateStore {
    override fun find(code: String): BatchedQualificationTemplate? {
        val hit = (cache.get(code) as BatchedQualificationTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedQualificationTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedQualificationTemplate> = inner.list()
}

fun newCachedBatchedQualificationTemplateStore(inner: BatchedQualificationTemplateStore, ttl: Int, clock: Clock): BatchedQualificationTemplateStore {
    return CachedBatchedQualificationTemplateStore(
        inner = inner,
        cache = newTtlCache<BatchedQualificationTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched qualification templates. Empty fields match all. */
data class BatchedQualificationTemplateQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedQualificationTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedQualificationTemplate>): MutableList<BatchedQualificationTemplate> {
        val matched = filter(items) { candidate: BatchedQualificationTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedQualificationTemplates(store: BatchedQualificationTemplateStore, query: BatchedQualificationTemplateQuery): MutableList<BatchedQualificationTemplate> {
    return query.apply(store.list())
}
