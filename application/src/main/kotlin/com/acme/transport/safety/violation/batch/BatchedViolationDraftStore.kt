package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched violation drafts by code. */
interface BatchedViolationDraftStore {
    fun find(code: String): BatchedViolationDraft?
    fun save(item: BatchedViolationDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedViolationDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedViolationDraftStore(
    var items: MutableMap<String, BatchedViolationDraft>,
    var order: MutableList<String>,
) : BatchedViolationDraftStore {
    override fun find(code: String): BatchedViolationDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedViolationDraft) {
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

    override fun list(): MutableList<BatchedViolationDraft> {
        val out = mutableListOf<BatchedViolationDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedViolationDraftStore(): BatchedViolationDraftStore {
    return MemoryBatchedViolationDraftStore(
        mutableMapOf<String, BatchedViolationDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedViolationDraftStore(
    var inner: BatchedViolationDraftStore,
    var cache: TtlCache<BatchedViolationDraft>,
    var hits: Int,
    var misses: Int,
) : BatchedViolationDraftStore {
    override fun find(code: String): BatchedViolationDraft? {
        val hit = (cache.get(code) as BatchedViolationDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedViolationDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedViolationDraft> = inner.list()
}

fun newCachedBatchedViolationDraftStore(inner: BatchedViolationDraftStore, ttl: Int, clock: Clock): BatchedViolationDraftStore {
    return CachedBatchedViolationDraftStore(
        inner = inner,
        cache = newTtlCache<BatchedViolationDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched violation drafts. Empty fields match all. */
data class BatchedViolationDraftQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedViolationDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedViolationDraft>): MutableList<BatchedViolationDraft> {
        val matched = filter(items) { candidate: BatchedViolationDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedViolationDrafts(store: BatchedViolationDraftStore, query: BatchedViolationDraftQuery): MutableList<BatchedViolationDraft> {
    return query.apply(store.list())
}
