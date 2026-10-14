package com.acme.transport.freightpay.audit.tier

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled audit bundles by code. */
interface ScheduledAuditBundleStore {
    fun find(code: String): ScheduledAuditBundle?
    fun save(item: ScheduledAuditBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAuditBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAuditBundleStore(
    var items: MutableMap<String, ScheduledAuditBundle>,
    var order: MutableList<String>,
) : ScheduledAuditBundleStore {
    override fun find(code: String): ScheduledAuditBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAuditBundle) {
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

    override fun list(): MutableList<ScheduledAuditBundle> {
        val out = mutableListOf<ScheduledAuditBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAuditBundleStore(): ScheduledAuditBundleStore {
    return MemoryScheduledAuditBundleStore(
        mutableMapOf<String, ScheduledAuditBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAuditBundleStore(
    var inner: ScheduledAuditBundleStore,
    var cache: TtlCache<ScheduledAuditBundle>,
    var hits: Int,
    var misses: Int,
) : ScheduledAuditBundleStore {
    override fun find(code: String): ScheduledAuditBundle? {
        val hit = (cache.get(code) as ScheduledAuditBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAuditBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAuditBundle> = inner.list()
}

fun newCachedScheduledAuditBundleStore(inner: ScheduledAuditBundleStore, ttl: Int, clock: Clock): ScheduledAuditBundleStore {
    return CachedScheduledAuditBundleStore(
        inner = inner,
        cache = newTtlCache<ScheduledAuditBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled audit bundles. Empty fields match all. */
data class ScheduledAuditBundleQuery(
    var unit: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAuditBundle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ScheduledAuditBundle>): MutableList<ScheduledAuditBundle> {
        val matched = filter(items) { candidate: ScheduledAuditBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAuditBundles(store: ScheduledAuditBundleStore, query: ScheduledAuditBundleQuery): MutableList<ScheduledAuditBundle> {
    return query.apply(store.list())
}
