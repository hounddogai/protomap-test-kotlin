package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node notices by code. */
interface NodeNoticeStore {
    fun find(code: String): NodeNotice?
    fun save(item: NodeNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeNoticeStore(
    var items: MutableMap<String, NodeNotice>,
    var order: MutableList<String>,
) : NodeNoticeStore {
    override fun find(code: String): NodeNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeNotice) {
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

    override fun list(): MutableList<NodeNotice> {
        val out = mutableListOf<NodeNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeNoticeStore(): NodeNoticeStore {
    return MemoryNodeNoticeStore(mutableMapOf<String, NodeNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeNoticeStore(
    var inner: NodeNoticeStore,
    var cache: TtlCache<NodeNotice>,
    var hits: Int,
    var misses: Int,
) : NodeNoticeStore {
    override fun find(code: String): NodeNotice? {
        val hit = (cache.get(code) as NodeNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeNotice> = inner.list()
}

fun newCachedNodeNoticeStore(inner: NodeNoticeStore, ttl: Int, clock: Clock): NodeNoticeStore {
    return CachedNodeNoticeStore(
        inner = inner,
        cache = newTtlCache<NodeNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node notices. Empty fields match all. */
data class NodeNoticeQuery(
    var source: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeNotice): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeNotice>): MutableList<NodeNotice> {
        val matched = filter(items) { candidate: NodeNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeNotices(store: NodeNoticeStore, query: NodeNoticeQuery): MutableList<NodeNotice> {
    return query.apply(store.list())
}
