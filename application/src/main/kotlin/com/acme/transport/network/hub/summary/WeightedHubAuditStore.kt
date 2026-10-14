package com.acme.transport.network.hub.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted hub audits by code. */
interface WeightedHubAuditStore {
    fun find(code: String): WeightedHubAudit?
    fun save(item: WeightedHubAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedHubAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedHubAuditStore(
    var items: MutableMap<String, WeightedHubAudit>,
    var order: MutableList<String>,
) : WeightedHubAuditStore {
    override fun find(code: String): WeightedHubAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedHubAudit) {
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

    override fun list(): MutableList<WeightedHubAudit> {
        val out = mutableListOf<WeightedHubAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedHubAuditStore(): WeightedHubAuditStore {
    return MemoryWeightedHubAuditStore(mutableMapOf<String, WeightedHubAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedHubAuditStore(
    var inner: WeightedHubAuditStore,
    var cache: TtlCache<WeightedHubAudit>,
    var hits: Int,
    var misses: Int,
) : WeightedHubAuditStore {
    override fun find(code: String): WeightedHubAudit? {
        val hit = (cache.get(code) as WeightedHubAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedHubAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedHubAudit> = inner.list()
}

fun newCachedWeightedHubAuditStore(inner: WeightedHubAuditStore, ttl: Int, clock: Clock): WeightedHubAuditStore {
    return CachedWeightedHubAuditStore(
        inner = inner,
        cache = newTtlCache<WeightedHubAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted hub audits. Empty fields match all. */
data class WeightedHubAuditQuery(
    var locale: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedHubAudit): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedHubAudit>): MutableList<WeightedHubAudit> {
        val matched = filter(items) { candidate: WeightedHubAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedHubAudits(store: WeightedHubAuditStore, query: WeightedHubAuditQuery): MutableList<WeightedHubAudit> {
    return query.apply(store.list())
}
