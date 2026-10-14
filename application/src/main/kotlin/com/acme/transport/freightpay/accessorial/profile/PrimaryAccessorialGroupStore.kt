package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary accessorial groups by code. */
interface PrimaryAccessorialGroupStore {
    fun find(code: String): PrimaryAccessorialGroup?
    fun save(item: PrimaryAccessorialGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAccessorialGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAccessorialGroupStore(
    var items: MutableMap<String, PrimaryAccessorialGroup>,
    var order: MutableList<String>,
) : PrimaryAccessorialGroupStore {
    override fun find(code: String): PrimaryAccessorialGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAccessorialGroup) {
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

    override fun list(): MutableList<PrimaryAccessorialGroup> {
        val out = mutableListOf<PrimaryAccessorialGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAccessorialGroupStore(): PrimaryAccessorialGroupStore {
    return MemoryPrimaryAccessorialGroupStore(
        mutableMapOf<String, PrimaryAccessorialGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAccessorialGroupStore(
    var inner: PrimaryAccessorialGroupStore,
    var cache: TtlCache<PrimaryAccessorialGroup>,
    var hits: Int,
    var misses: Int,
) : PrimaryAccessorialGroupStore {
    override fun find(code: String): PrimaryAccessorialGroup? {
        val hit = (cache.get(code) as PrimaryAccessorialGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAccessorialGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAccessorialGroup> = inner.list()
}

fun newCachedPrimaryAccessorialGroupStore(inner: PrimaryAccessorialGroupStore, ttl: Int, clock: Clock): PrimaryAccessorialGroupStore {
    return CachedPrimaryAccessorialGroupStore(
        inner = inner,
        cache = newTtlCache<PrimaryAccessorialGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary accessorial groups. Empty fields match all. */
data class PrimaryAccessorialGroupQuery(
    var region: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAccessorialGroup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<PrimaryAccessorialGroup>): MutableList<PrimaryAccessorialGroup> {
        val matched = filter(items) { candidate: PrimaryAccessorialGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAccessorialGroups(store: PrimaryAccessorialGroupStore, query: PrimaryAccessorialGroupQuery): MutableList<PrimaryAccessorialGroup> {
    return query.apply(store.list())
}
