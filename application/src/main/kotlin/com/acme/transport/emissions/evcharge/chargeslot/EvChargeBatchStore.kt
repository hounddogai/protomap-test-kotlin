package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge batches by code. */
interface EvChargeBatchStore {
    fun find(code: String): EvChargeBatch?
    fun save(item: EvChargeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeBatchStore(
    var items: MutableMap<String, EvChargeBatch>,
    var order: MutableList<String>,
) : EvChargeBatchStore {
    override fun find(code: String): EvChargeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeBatch) {
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

    override fun list(): MutableList<EvChargeBatch> {
        val out = mutableListOf<EvChargeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeBatchStore(): EvChargeBatchStore {
    return MemoryEvChargeBatchStore(mutableMapOf<String, EvChargeBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeBatchStore(
    var inner: EvChargeBatchStore,
    var cache: TtlCache<EvChargeBatch>,
    var hits: Int,
    var misses: Int,
) : EvChargeBatchStore {
    override fun find(code: String): EvChargeBatch? {
        val hit = (cache.get(code) as EvChargeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeBatch> = inner.list()
}

fun newCachedEvChargeBatchStore(inner: EvChargeBatchStore, ttl: Int, clock: Clock): EvChargeBatchStore {
    return CachedEvChargeBatchStore(
        inner = inner,
        cache = newTtlCache<EvChargeBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge batches. Empty fields match all. */
data class EvChargeBatchQuery(
    var region: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeBatch): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeBatch>): MutableList<EvChargeBatch> {
        val matched = filter(items) { candidate: EvChargeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeBatches(store: EvChargeBatchStore, query: EvChargeBatchQuery): MutableList<EvChargeBatch> {
    return query.apply(store.list())
}
