package com.acme.transport.customs.broker.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker groups by code. */
interface BrokerGroupStore {
    fun find(code: String): BrokerGroup?
    fun save(item: BrokerGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerGroupStore(
    var items: MutableMap<String, BrokerGroup>,
    var order: MutableList<String>,
) : BrokerGroupStore {
    override fun find(code: String): BrokerGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerGroup) {
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

    override fun list(): MutableList<BrokerGroup> {
        val out = mutableListOf<BrokerGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerGroupStore(): BrokerGroupStore {
    return MemoryBrokerGroupStore(mutableMapOf<String, BrokerGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerGroupStore(
    var inner: BrokerGroupStore,
    var cache: TtlCache<BrokerGroup>,
    var hits: Int,
    var misses: Int,
) : BrokerGroupStore {
    override fun find(code: String): BrokerGroup? {
        val hit = (cache.get(code) as BrokerGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerGroup> = inner.list()
}

fun newCachedBrokerGroupStore(inner: BrokerGroupStore, ttl: Int, clock: Clock): BrokerGroupStore {
    return CachedBrokerGroupStore(
        inner = inner,
        cache = newTtlCache<BrokerGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker groups. Empty fields match all. */
data class BrokerGroupQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerGroup>): MutableList<BrokerGroup> {
        val matched = filter(items) { candidate: BrokerGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerGroups(store: BrokerGroupStore, query: BrokerGroupQuery): MutableList<BrokerGroup> {
    return query.apply(store.list())
}
