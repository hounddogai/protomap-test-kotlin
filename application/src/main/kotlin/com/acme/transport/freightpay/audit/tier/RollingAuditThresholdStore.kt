package com.acme.transport.freightpay.audit.tier

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling audit thresholds by code. */
interface RollingAuditThresholdStore {
    fun find(code: String): RollingAuditThreshold?
    fun save(item: RollingAuditThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAuditThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAuditThresholdStore(
    var items: MutableMap<String, RollingAuditThreshold>,
    var order: MutableList<String>,
) : RollingAuditThresholdStore {
    override fun find(code: String): RollingAuditThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAuditThreshold) {
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

    override fun list(): MutableList<RollingAuditThreshold> {
        val out = mutableListOf<RollingAuditThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAuditThresholdStore(): RollingAuditThresholdStore {
    return MemoryRollingAuditThresholdStore(
        mutableMapOf<String, RollingAuditThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAuditThresholdStore(
    var inner: RollingAuditThresholdStore,
    var cache: TtlCache<RollingAuditThreshold>,
    var hits: Int,
    var misses: Int,
) : RollingAuditThresholdStore {
    override fun find(code: String): RollingAuditThreshold? {
        val hit = (cache.get(code) as RollingAuditThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAuditThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAuditThreshold> = inner.list()
}

fun newCachedRollingAuditThresholdStore(inner: RollingAuditThresholdStore, ttl: Int, clock: Clock): RollingAuditThresholdStore {
    return CachedRollingAuditThresholdStore(
        inner = inner,
        cache = newTtlCache<RollingAuditThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling audit thresholds. Empty fields match all. */
data class RollingAuditThresholdQuery(
    var unit: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAuditThreshold): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAuditThreshold>): MutableList<RollingAuditThreshold> {
        val matched = filter(items) { candidate: RollingAuditThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAuditThresholds(store: RollingAuditThresholdStore, query: RollingAuditThresholdQuery): MutableList<RollingAuditThreshold> {
    return query.apply(store.list())
}
