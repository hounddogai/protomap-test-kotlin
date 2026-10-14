package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual dispute bundles by code. */
interface ManualDisputeBundleStore {
    fun find(code: String): ManualDisputeBundle?
    fun save(item: ManualDisputeBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDisputeBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDisputeBundleStore(
    var items: MutableMap<String, ManualDisputeBundle>,
    var order: MutableList<String>,
) : ManualDisputeBundleStore {
    override fun find(code: String): ManualDisputeBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDisputeBundle) {
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

    override fun list(): MutableList<ManualDisputeBundle> {
        val out = mutableListOf<ManualDisputeBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDisputeBundleStore(): ManualDisputeBundleStore {
    return MemoryManualDisputeBundleStore(mutableMapOf<String, ManualDisputeBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDisputeBundleStore(
    var inner: ManualDisputeBundleStore,
    var cache: TtlCache<ManualDisputeBundle>,
    var hits: Int,
    var misses: Int,
) : ManualDisputeBundleStore {
    override fun find(code: String): ManualDisputeBundle? {
        val hit = (cache.get(code) as ManualDisputeBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDisputeBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDisputeBundle> = inner.list()
}

fun newCachedManualDisputeBundleStore(inner: ManualDisputeBundleStore, ttl: Int, clock: Clock): ManualDisputeBundleStore {
    return CachedManualDisputeBundleStore(
        inner = inner,
        cache = newTtlCache<ManualDisputeBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual dispute bundles. Empty fields match all. */
data class ManualDisputeBundleQuery(
    var region: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDisputeBundle): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDisputeBundle>): MutableList<ManualDisputeBundle> {
        val matched = filter(items) { candidate: ManualDisputeBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDisputeBundles(store: ManualDisputeBundleStore, query: ManualDisputeBundleQuery): MutableList<ManualDisputeBundle> {
    return query.apply(store.list())
}
