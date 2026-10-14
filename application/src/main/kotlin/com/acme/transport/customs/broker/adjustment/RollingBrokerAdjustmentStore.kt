package com.acme.transport.customs.broker.adjustment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling broker adjustments by code. */
interface RollingBrokerAdjustmentStore {
    fun find(code: String): RollingBrokerAdjustment?
    fun save(item: RollingBrokerAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingBrokerAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingBrokerAdjustmentStore(
    var items: MutableMap<String, RollingBrokerAdjustment>,
    var order: MutableList<String>,
) : RollingBrokerAdjustmentStore {
    override fun find(code: String): RollingBrokerAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingBrokerAdjustment) {
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

    override fun list(): MutableList<RollingBrokerAdjustment> {
        val out = mutableListOf<RollingBrokerAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingBrokerAdjustmentStore(): RollingBrokerAdjustmentStore {
    return MemoryRollingBrokerAdjustmentStore(
        mutableMapOf<String, RollingBrokerAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingBrokerAdjustmentStore(
    var inner: RollingBrokerAdjustmentStore,
    var cache: TtlCache<RollingBrokerAdjustment>,
    var hits: Int,
    var misses: Int,
) : RollingBrokerAdjustmentStore {
    override fun find(code: String): RollingBrokerAdjustment? {
        val hit = (cache.get(code) as RollingBrokerAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingBrokerAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingBrokerAdjustment> = inner.list()
}

fun newCachedRollingBrokerAdjustmentStore(inner: RollingBrokerAdjustmentStore, ttl: Int, clock: Clock): RollingBrokerAdjustmentStore {
    return CachedRollingBrokerAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RollingBrokerAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling broker adjustments. Empty fields match all. */
data class RollingBrokerAdjustmentQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingBrokerAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RollingBrokerAdjustment>): MutableList<RollingBrokerAdjustment> {
        val matched = filter(items) { candidate: RollingBrokerAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingBrokerAdjustments(store: RollingBrokerAdjustmentStore, query: RollingBrokerAdjustmentQuery): MutableList<RollingBrokerAdjustment> {
    return query.apply(store.list())
}
