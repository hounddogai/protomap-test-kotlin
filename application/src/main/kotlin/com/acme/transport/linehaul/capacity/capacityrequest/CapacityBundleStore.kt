package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity bundles by code. */
interface CapacityBundleStore {
    fun find(code: String): CapacityBundle?
    fun save(item: CapacityBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityBundleStore(
    var items: MutableMap<String, CapacityBundle>,
    var order: MutableList<String>,
) : CapacityBundleStore {
    override fun find(code: String): CapacityBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityBundle) {
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

    override fun list(): MutableList<CapacityBundle> {
        val out = mutableListOf<CapacityBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityBundleStore(): CapacityBundleStore {
    return MemoryCapacityBundleStore(mutableMapOf<String, CapacityBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityBundleStore(
    var inner: CapacityBundleStore,
    var cache: TtlCache<CapacityBundle>,
    var hits: Int,
    var misses: Int,
) : CapacityBundleStore {
    override fun find(code: String): CapacityBundle? {
        val hit = (cache.get(code) as CapacityBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityBundle> = inner.list()
}

fun newCachedCapacityBundleStore(inner: CapacityBundleStore, ttl: Int, clock: Clock): CapacityBundleStore {
    return CachedCapacityBundleStore(
        inner = inner,
        cache = newTtlCache<CapacityBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity bundles. Empty fields match all. */
data class CapacityBundleQuery(
    var source: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityBundle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityBundle>): MutableList<CapacityBundle> {
        val matched = filter(items) { candidate: CapacityBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityBundles(store: CapacityBundleStore, query: CapacityBundleQuery): MutableList<CapacityBundle> {
    return query.apply(store.list())
}
