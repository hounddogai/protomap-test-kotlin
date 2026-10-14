package com.acme.transport.network.node.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node drafts by code. */
interface NodeDraftStore {
    fun find(code: String): NodeDraft?
    fun save(item: NodeDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeDraftStore(
    var items: MutableMap<String, NodeDraft>,
    var order: MutableList<String>,
) : NodeDraftStore {
    override fun find(code: String): NodeDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeDraft) {
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

    override fun list(): MutableList<NodeDraft> {
        val out = mutableListOf<NodeDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeDraftStore(): NodeDraftStore {
    return MemoryNodeDraftStore(mutableMapOf<String, NodeDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeDraftStore(
    var inner: NodeDraftStore,
    var cache: TtlCache<NodeDraft>,
    var hits: Int,
    var misses: Int,
) : NodeDraftStore {
    override fun find(code: String): NodeDraft? {
        val hit = (cache.get(code) as NodeDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeDraft> = inner.list()
}

fun newCachedNodeDraftStore(inner: NodeDraftStore, ttl: Int, clock: Clock): NodeDraftStore {
    return CachedNodeDraftStore(
        inner = inner,
        cache = newTtlCache<NodeDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node drafts. Empty fields match all. */
data class NodeDraftQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeDraft>): MutableList<NodeDraft> {
        val matched = filter(items) { candidate: NodeDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeDrafts(store: NodeDraftStore, query: NodeDraftQuery): MutableList<NodeDraft> {
    return query.apply(store.list())
}
