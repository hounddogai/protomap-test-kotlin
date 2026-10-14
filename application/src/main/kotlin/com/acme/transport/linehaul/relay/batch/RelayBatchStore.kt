package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay batches by code. */
interface RelayBatchStore {
    fun find(code: String): RelayBatch?
    fun save(item: RelayBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayBatchStore(
    var items: MutableMap<String, RelayBatch>,
    var order: MutableList<String>,
) : RelayBatchStore {
    override fun find(code: String): RelayBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayBatch) {
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

    override fun list(): MutableList<RelayBatch> {
        val out = mutableListOf<RelayBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayBatchStore(): RelayBatchStore {
    return MemoryRelayBatchStore(mutableMapOf<String, RelayBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayBatchStore(
    var inner: RelayBatchStore,
    var cache: TtlCache<RelayBatch>,
    var hits: Int,
    var misses: Int,
) : RelayBatchStore {
    override fun find(code: String): RelayBatch? {
        val hit = (cache.get(code) as RelayBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayBatch> = inner.list()
}

fun newCachedRelayBatchStore(inner: RelayBatchStore, ttl: Int, clock: Clock): RelayBatchStore {
    return CachedRelayBatchStore(
        inner = inner,
        cache = newTtlCache<RelayBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay batches. Empty fields match all. */
data class RelayBatchQuery(
    var country: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayBatch): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayBatch>): MutableList<RelayBatch> {
        val matched = filter(items) { candidate: RelayBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayBatches(store: RelayBatchStore, query: RelayBatchQuery): MutableList<RelayBatch> {
    return query.apply(store.list())
}
