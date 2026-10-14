package com.acme.transport.customs.hold.audit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict hold bundles by code. */
interface StrictHoldBundleStore {
    fun find(code: String): StrictHoldBundle?
    fun save(item: StrictHoldBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictHoldBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictHoldBundleStore(
    var items: MutableMap<String, StrictHoldBundle>,
    var order: MutableList<String>,
) : StrictHoldBundleStore {
    override fun find(code: String): StrictHoldBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictHoldBundle) {
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

    override fun list(): MutableList<StrictHoldBundle> {
        val out = mutableListOf<StrictHoldBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictHoldBundleStore(): StrictHoldBundleStore {
    return MemoryStrictHoldBundleStore(mutableMapOf<String, StrictHoldBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictHoldBundleStore(
    var inner: StrictHoldBundleStore,
    var cache: TtlCache<StrictHoldBundle>,
    var hits: Int,
    var misses: Int,
) : StrictHoldBundleStore {
    override fun find(code: String): StrictHoldBundle? {
        val hit = (cache.get(code) as StrictHoldBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictHoldBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictHoldBundle> = inner.list()
}

fun newCachedStrictHoldBundleStore(inner: StrictHoldBundleStore, ttl: Int, clock: Clock): StrictHoldBundleStore {
    return CachedStrictHoldBundleStore(
        inner = inner,
        cache = newTtlCache<StrictHoldBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict hold bundles. Empty fields match all. */
data class StrictHoldBundleQuery(
    var unit: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictHoldBundle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<StrictHoldBundle>): MutableList<StrictHoldBundle> {
        val matched = filter(items) { candidate: StrictHoldBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictHoldBundles(store: StrictHoldBundleStore, query: StrictHoldBundleQuery): MutableList<StrictHoldBundle> {
    return query.apply(store.list())
}
