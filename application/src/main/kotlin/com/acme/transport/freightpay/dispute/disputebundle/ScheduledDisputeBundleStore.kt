package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled dispute bundles by code. */
interface ScheduledDisputeBundleStore {
    fun find(code: String): ScheduledDisputeBundle?
    fun save(item: ScheduledDisputeBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDisputeBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDisputeBundleStore(
    var items: MutableMap<String, ScheduledDisputeBundle>,
    var order: MutableList<String>,
) : ScheduledDisputeBundleStore {
    override fun find(code: String): ScheduledDisputeBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDisputeBundle) {
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

    override fun list(): MutableList<ScheduledDisputeBundle> {
        val out = mutableListOf<ScheduledDisputeBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDisputeBundleStore(): ScheduledDisputeBundleStore {
    return MemoryScheduledDisputeBundleStore(
        mutableMapOf<String, ScheduledDisputeBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDisputeBundleStore(
    var inner: ScheduledDisputeBundleStore,
    var cache: TtlCache<ScheduledDisputeBundle>,
    var hits: Int,
    var misses: Int,
) : ScheduledDisputeBundleStore {
    override fun find(code: String): ScheduledDisputeBundle? {
        val hit = (cache.get(code) as ScheduledDisputeBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDisputeBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDisputeBundle> = inner.list()
}

fun newCachedScheduledDisputeBundleStore(inner: ScheduledDisputeBundleStore, ttl: Int, clock: Clock): ScheduledDisputeBundleStore {
    return CachedScheduledDisputeBundleStore(
        inner = inner,
        cache = newTtlCache<ScheduledDisputeBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dispute bundles. Empty fields match all. */
data class ScheduledDisputeBundleQuery(
    var category: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDisputeBundle): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDisputeBundle>): MutableList<ScheduledDisputeBundle> {
        val matched = filter(items) { candidate: ScheduledDisputeBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDisputeBundles(store: ScheduledDisputeBundleStore, query: ScheduledDisputeBundleQuery): MutableList<ScheduledDisputeBundle> {
    return query.apply(store.list())
}
