package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute bundles by code. */
interface DisputeBundleStore {
    fun find(code: String): DisputeBundle?
    fun save(item: DisputeBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeBundleStore(
    var items: MutableMap<String, DisputeBundle>,
    var order: MutableList<String>,
) : DisputeBundleStore {
    override fun find(code: String): DisputeBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeBundle) {
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

    override fun list(): MutableList<DisputeBundle> {
        val out = mutableListOf<DisputeBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeBundleStore(): DisputeBundleStore {
    return MemoryDisputeBundleStore(mutableMapOf<String, DisputeBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeBundleStore(
    var inner: DisputeBundleStore,
    var cache: TtlCache<DisputeBundle>,
    var hits: Int,
    var misses: Int,
) : DisputeBundleStore {
    override fun find(code: String): DisputeBundle? {
        val hit = (cache.get(code) as DisputeBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeBundle> = inner.list()
}

fun newCachedDisputeBundleStore(inner: DisputeBundleStore, ttl: Int, clock: Clock): DisputeBundleStore {
    return CachedDisputeBundleStore(
        inner = inner,
        cache = newTtlCache<DisputeBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute bundles. Empty fields match all. */
data class DisputeBundleQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeBundle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeBundle>): MutableList<DisputeBundle> {
        val matched = filter(items) { candidate: DisputeBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeBundles(store: DisputeBundleStore, query: DisputeBundleQuery): MutableList<DisputeBundle> {
    return query.apply(store.list())
}
