package com.acme.transport.network.node.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node reviews by code. */
interface NodeReviewStore {
    fun find(code: String): NodeReview?
    fun save(item: NodeReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeReviewStore(
    var items: MutableMap<String, NodeReview>,
    var order: MutableList<String>,
) : NodeReviewStore {
    override fun find(code: String): NodeReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeReview) {
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

    override fun list(): MutableList<NodeReview> {
        val out = mutableListOf<NodeReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeReviewStore(): NodeReviewStore {
    return MemoryNodeReviewStore(mutableMapOf<String, NodeReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeReviewStore(
    var inner: NodeReviewStore,
    var cache: TtlCache<NodeReview>,
    var hits: Int,
    var misses: Int,
) : NodeReviewStore {
    override fun find(code: String): NodeReview? {
        val hit = (cache.get(code) as NodeReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeReview> = inner.list()
}

fun newCachedNodeReviewStore(inner: NodeReviewStore, ttl: Int, clock: Clock): NodeReviewStore {
    return CachedNodeReviewStore(
        inner = inner,
        cache = newTtlCache<NodeReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node reviews. Empty fields match all. */
data class NodeReviewQuery(
    var depot: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeReview): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<NodeReview>): MutableList<NodeReview> {
        val matched = filter(items) { candidate: NodeReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeReviews(store: NodeReviewStore, query: NodeReviewQuery): MutableList<NodeReview> {
    return query.apply(store.list())
}
