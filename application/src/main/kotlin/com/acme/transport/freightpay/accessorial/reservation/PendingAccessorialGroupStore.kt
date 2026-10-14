package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending accessorial groups by code. */
interface PendingAccessorialGroupStore {
    fun find(code: String): PendingAccessorialGroup?
    fun save(item: PendingAccessorialGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAccessorialGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAccessorialGroupStore(
    var items: MutableMap<String, PendingAccessorialGroup>,
    var order: MutableList<String>,
) : PendingAccessorialGroupStore {
    override fun find(code: String): PendingAccessorialGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAccessorialGroup) {
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

    override fun list(): MutableList<PendingAccessorialGroup> {
        val out = mutableListOf<PendingAccessorialGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAccessorialGroupStore(): PendingAccessorialGroupStore {
    return MemoryPendingAccessorialGroupStore(
        mutableMapOf<String, PendingAccessorialGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAccessorialGroupStore(
    var inner: PendingAccessorialGroupStore,
    var cache: TtlCache<PendingAccessorialGroup>,
    var hits: Int,
    var misses: Int,
) : PendingAccessorialGroupStore {
    override fun find(code: String): PendingAccessorialGroup? {
        val hit = (cache.get(code) as PendingAccessorialGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAccessorialGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAccessorialGroup> = inner.list()
}

fun newCachedPendingAccessorialGroupStore(inner: PendingAccessorialGroupStore, ttl: Int, clock: Clock): PendingAccessorialGroupStore {
    return CachedPendingAccessorialGroupStore(
        inner = inner,
        cache = newTtlCache<PendingAccessorialGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending accessorial groups. Empty fields match all. */
data class PendingAccessorialGroupQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAccessorialGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingAccessorialGroup>): MutableList<PendingAccessorialGroup> {
        val matched = filter(items) { candidate: PendingAccessorialGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAccessorialGroups(store: PendingAccessorialGroupStore, query: PendingAccessorialGroupQuery): MutableList<PendingAccessorialGroup> {
    return query.apply(store.list())
}
