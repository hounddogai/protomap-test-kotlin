package com.acme.transport.crewing.swap.notice

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched swap notices by code. */
interface BatchedSwapNoticeStore {
    fun find(code: String): BatchedSwapNotice?
    fun save(item: BatchedSwapNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedSwapNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedSwapNoticeStore(
    var items: MutableMap<String, BatchedSwapNotice>,
    var order: MutableList<String>,
) : BatchedSwapNoticeStore {
    override fun find(code: String): BatchedSwapNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedSwapNotice) {
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

    override fun list(): MutableList<BatchedSwapNotice> {
        val out = mutableListOf<BatchedSwapNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedSwapNoticeStore(): BatchedSwapNoticeStore {
    return MemoryBatchedSwapNoticeStore(mutableMapOf<String, BatchedSwapNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedSwapNoticeStore(
    var inner: BatchedSwapNoticeStore,
    var cache: TtlCache<BatchedSwapNotice>,
    var hits: Int,
    var misses: Int,
) : BatchedSwapNoticeStore {
    override fun find(code: String): BatchedSwapNotice? {
        val hit = (cache.get(code) as BatchedSwapNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedSwapNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedSwapNotice> = inner.list()
}

fun newCachedBatchedSwapNoticeStore(inner: BatchedSwapNoticeStore, ttl: Int, clock: Clock): BatchedSwapNoticeStore {
    return CachedBatchedSwapNoticeStore(
        inner = inner,
        cache = newTtlCache<BatchedSwapNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched swap notices. Empty fields match all. */
data class BatchedSwapNoticeQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedSwapNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedSwapNotice>): MutableList<BatchedSwapNotice> {
        val matched = filter(items) { candidate: BatchedSwapNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedSwapNotices(store: BatchedSwapNoticeStore, query: BatchedSwapNoticeQuery): MutableList<BatchedSwapNotice> {
    return query.apply(store.list())
}
