package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global relay bundles by code. */
interface GlobalRelayBundleStore {
    fun find(code: String): GlobalRelayBundle?
    fun save(item: GlobalRelayBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRelayBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRelayBundleStore(
    var items: MutableMap<String, GlobalRelayBundle>,
    var order: MutableList<String>,
) : GlobalRelayBundleStore {
    override fun find(code: String): GlobalRelayBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRelayBundle) {
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

    override fun list(): MutableList<GlobalRelayBundle> {
        val out = mutableListOf<GlobalRelayBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRelayBundleStore(): GlobalRelayBundleStore {
    return MemoryGlobalRelayBundleStore(mutableMapOf<String, GlobalRelayBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRelayBundleStore(
    var inner: GlobalRelayBundleStore,
    var cache: TtlCache<GlobalRelayBundle>,
    var hits: Int,
    var misses: Int,
) : GlobalRelayBundleStore {
    override fun find(code: String): GlobalRelayBundle? {
        val hit = (cache.get(code) as GlobalRelayBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRelayBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRelayBundle> = inner.list()
}

fun newCachedGlobalRelayBundleStore(inner: GlobalRelayBundleStore, ttl: Int, clock: Clock): GlobalRelayBundleStore {
    return CachedGlobalRelayBundleStore(
        inner = inner,
        cache = newTtlCache<GlobalRelayBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global relay bundles. Empty fields match all. */
data class GlobalRelayBundleQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRelayBundle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRelayBundle>): MutableList<GlobalRelayBundle> {
        val matched = filter(items) { candidate: GlobalRelayBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRelayBundles(store: GlobalRelayBundleStore, query: GlobalRelayBundleQuery): MutableList<GlobalRelayBundle> {
    return query.apply(store.list())
}
