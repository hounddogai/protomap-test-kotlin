package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive audit bundles by code. */
interface AdaptiveAuditBundleStore {
    fun find(code: String): AdaptiveAuditBundle?
    fun save(item: AdaptiveAuditBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAuditBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAuditBundleStore(
    var items: MutableMap<String, AdaptiveAuditBundle>,
    var order: MutableList<String>,
) : AdaptiveAuditBundleStore {
    override fun find(code: String): AdaptiveAuditBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAuditBundle) {
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

    override fun list(): MutableList<AdaptiveAuditBundle> {
        val out = mutableListOf<AdaptiveAuditBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAuditBundleStore(): AdaptiveAuditBundleStore {
    return MemoryAdaptiveAuditBundleStore(mutableMapOf<String, AdaptiveAuditBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAuditBundleStore(
    var inner: AdaptiveAuditBundleStore,
    var cache: TtlCache<AdaptiveAuditBundle>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAuditBundleStore {
    override fun find(code: String): AdaptiveAuditBundle? {
        val hit = (cache.get(code) as AdaptiveAuditBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAuditBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAuditBundle> = inner.list()
}

fun newCachedAdaptiveAuditBundleStore(inner: AdaptiveAuditBundleStore, ttl: Int, clock: Clock): AdaptiveAuditBundleStore {
    return CachedAdaptiveAuditBundleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAuditBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive audit bundles. Empty fields match all. */
data class AdaptiveAuditBundleQuery(
    var channel: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAuditBundle): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAuditBundle>): MutableList<AdaptiveAuditBundle> {
        val matched = filter(items) { candidate: AdaptiveAuditBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAuditBundles(store: AdaptiveAuditBundleStore, query: AdaptiveAuditBundleQuery): MutableList<AdaptiveAuditBundle> {
    return query.apply(store.list())
}
