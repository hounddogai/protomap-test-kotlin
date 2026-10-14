package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global dispute batches by code. */
interface GlobalDisputeBatchStore {
    fun find(code: String): GlobalDisputeBatch?
    fun save(item: GlobalDisputeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDisputeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDisputeBatchStore(
    var items: MutableMap<String, GlobalDisputeBatch>,
    var order: MutableList<String>,
) : GlobalDisputeBatchStore {
    override fun find(code: String): GlobalDisputeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDisputeBatch) {
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

    override fun list(): MutableList<GlobalDisputeBatch> {
        val out = mutableListOf<GlobalDisputeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDisputeBatchStore(): GlobalDisputeBatchStore {
    return MemoryGlobalDisputeBatchStore(mutableMapOf<String, GlobalDisputeBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDisputeBatchStore(
    var inner: GlobalDisputeBatchStore,
    var cache: TtlCache<GlobalDisputeBatch>,
    var hits: Int,
    var misses: Int,
) : GlobalDisputeBatchStore {
    override fun find(code: String): GlobalDisputeBatch? {
        val hit = (cache.get(code) as GlobalDisputeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDisputeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDisputeBatch> = inner.list()
}

fun newCachedGlobalDisputeBatchStore(inner: GlobalDisputeBatchStore, ttl: Int, clock: Clock): GlobalDisputeBatchStore {
    return CachedGlobalDisputeBatchStore(
        inner = inner,
        cache = newTtlCache<GlobalDisputeBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global dispute batches. Empty fields match all. */
data class GlobalDisputeBatchQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDisputeBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDisputeBatch>): MutableList<GlobalDisputeBatch> {
        val matched = filter(items) { candidate: GlobalDisputeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDisputeBatches(store: GlobalDisputeBatchStore, query: GlobalDisputeBatchQuery): MutableList<GlobalDisputeBatch> {
    return query.apply(store.list())
}
