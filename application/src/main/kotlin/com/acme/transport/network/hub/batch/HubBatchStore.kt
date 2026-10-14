package com.acme.transport.network.hub.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub batches by code. */
interface HubBatchStore {
    fun find(code: String): HubBatch?
    fun save(item: HubBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubBatchStore(
    var items: MutableMap<String, HubBatch>,
    var order: MutableList<String>,
) : HubBatchStore {
    override fun find(code: String): HubBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubBatch) {
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

    override fun list(): MutableList<HubBatch> {
        val out = mutableListOf<HubBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubBatchStore(): HubBatchStore {
    return MemoryHubBatchStore(mutableMapOf<String, HubBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubBatchStore(
    var inner: HubBatchStore,
    var cache: TtlCache<HubBatch>,
    var hits: Int,
    var misses: Int,
) : HubBatchStore {
    override fun find(code: String): HubBatch? {
        val hit = (cache.get(code) as HubBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubBatch> = inner.list()
}

fun newCachedHubBatchStore(inner: HubBatchStore, ttl: Int, clock: Clock): HubBatchStore {
    return CachedHubBatchStore(
        inner = inner,
        cache = newTtlCache<HubBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub batches. Empty fields match all. */
data class HubBatchQuery(
    var channel: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubBatch): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<HubBatch>): MutableList<HubBatch> {
        val matched = filter(items) { candidate: HubBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubBatches(store: HubBatchStore, query: HubBatchQuery): MutableList<HubBatch> = query.apply(store.list())
