package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit variances by code. */
interface AuditVarianceStore {
    fun find(code: String): AuditVariance?
    fun save(item: AuditVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditVarianceStore(
    var items: MutableMap<String, AuditVariance>,
    var order: MutableList<String>,
) : AuditVarianceStore {
    override fun find(code: String): AuditVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditVariance) {
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

    override fun list(): MutableList<AuditVariance> {
        val out = mutableListOf<AuditVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditVarianceStore(): AuditVarianceStore {
    return MemoryAuditVarianceStore(mutableMapOf<String, AuditVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditVarianceStore(
    var inner: AuditVarianceStore,
    var cache: TtlCache<AuditVariance>,
    var hits: Int,
    var misses: Int,
) : AuditVarianceStore {
    override fun find(code: String): AuditVariance? {
        val hit = (cache.get(code) as AuditVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditVariance> = inner.list()
}

fun newCachedAuditVarianceStore(inner: AuditVarianceStore, ttl: Int, clock: Clock): AuditVarianceStore {
    return CachedAuditVarianceStore(
        inner = inner,
        cache = newTtlCache<AuditVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit variances. Empty fields match all. */
data class AuditVarianceQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditVariance): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditVariance>): MutableList<AuditVariance> {
        val matched = filter(items) { candidate: AuditVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditVariances(store: AuditVarianceStore, query: AuditVarianceQuery): MutableList<AuditVariance> {
    return query.apply(store.list())
}
