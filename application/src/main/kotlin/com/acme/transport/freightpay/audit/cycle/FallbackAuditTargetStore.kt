package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback audit targets by code. */
interface FallbackAuditTargetStore {
    fun find(code: String): FallbackAuditTarget?
    fun save(item: FallbackAuditTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAuditTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAuditTargetStore(
    var items: MutableMap<String, FallbackAuditTarget>,
    var order: MutableList<String>,
) : FallbackAuditTargetStore {
    override fun find(code: String): FallbackAuditTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAuditTarget) {
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

    override fun list(): MutableList<FallbackAuditTarget> {
        val out = mutableListOf<FallbackAuditTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAuditTargetStore(): FallbackAuditTargetStore {
    return MemoryFallbackAuditTargetStore(mutableMapOf<String, FallbackAuditTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAuditTargetStore(
    var inner: FallbackAuditTargetStore,
    var cache: TtlCache<FallbackAuditTarget>,
    var hits: Int,
    var misses: Int,
) : FallbackAuditTargetStore {
    override fun find(code: String): FallbackAuditTarget? {
        val hit = (cache.get(code) as FallbackAuditTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAuditTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAuditTarget> = inner.list()
}

fun newCachedFallbackAuditTargetStore(inner: FallbackAuditTargetStore, ttl: Int, clock: Clock): FallbackAuditTargetStore {
    return CachedFallbackAuditTargetStore(
        inner = inner,
        cache = newTtlCache<FallbackAuditTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback audit targets. Empty fields match all. */
data class FallbackAuditTargetQuery(
    var region: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAuditTarget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAuditTarget>): MutableList<FallbackAuditTarget> {
        val matched = filter(items) { candidate: FallbackAuditTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAuditTargets(store: FallbackAuditTargetStore, query: FallbackAuditTargetQuery): MutableList<FallbackAuditTarget> {
    return query.apply(store.list())
}
