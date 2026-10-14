package com.acme.transport.customs.broker.audit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual broker overrides by code. */
interface ManualBrokerOverrideStore {
    fun find(code: String): ManualBrokerOverride?
    fun save(item: ManualBrokerOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualBrokerOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualBrokerOverrideStore(
    var items: MutableMap<String, ManualBrokerOverride>,
    var order: MutableList<String>,
) : ManualBrokerOverrideStore {
    override fun find(code: String): ManualBrokerOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualBrokerOverride) {
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

    override fun list(): MutableList<ManualBrokerOverride> {
        val out = mutableListOf<ManualBrokerOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualBrokerOverrideStore(): ManualBrokerOverrideStore {
    return MemoryManualBrokerOverrideStore(
        mutableMapOf<String, ManualBrokerOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualBrokerOverrideStore(
    var inner: ManualBrokerOverrideStore,
    var cache: TtlCache<ManualBrokerOverride>,
    var hits: Int,
    var misses: Int,
) : ManualBrokerOverrideStore {
    override fun find(code: String): ManualBrokerOverride? {
        val hit = (cache.get(code) as ManualBrokerOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualBrokerOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualBrokerOverride> = inner.list()
}

fun newCachedManualBrokerOverrideStore(inner: ManualBrokerOverrideStore, ttl: Int, clock: Clock): ManualBrokerOverrideStore {
    return CachedManualBrokerOverrideStore(
        inner = inner,
        cache = newTtlCache<ManualBrokerOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual broker overrides. Empty fields match all. */
data class ManualBrokerOverrideQuery(
    var unit: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualBrokerOverride): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualBrokerOverride>): MutableList<ManualBrokerOverride> {
        val matched = filter(items) { candidate: ManualBrokerOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualBrokerOverrides(store: ManualBrokerOverrideStore, query: ManualBrokerOverrideQuery): MutableList<ManualBrokerOverride> {
    return query.apply(store.list())
}
