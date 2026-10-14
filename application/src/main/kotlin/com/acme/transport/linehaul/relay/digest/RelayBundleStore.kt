package com.acme.transport.linehaul.relay.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay bundles by code. */
interface RelayBundleStore {
    fun find(code: String): RelayBundle?
    fun save(item: RelayBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayBundleStore(
    var items: MutableMap<String, RelayBundle>,
    var order: MutableList<String>,
) : RelayBundleStore {
    override fun find(code: String): RelayBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayBundle) {
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

    override fun list(): MutableList<RelayBundle> {
        val out = mutableListOf<RelayBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayBundleStore(): RelayBundleStore {
    return MemoryRelayBundleStore(mutableMapOf<String, RelayBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayBundleStore(
    var inner: RelayBundleStore,
    var cache: TtlCache<RelayBundle>,
    var hits: Int,
    var misses: Int,
) : RelayBundleStore {
    override fun find(code: String): RelayBundle? {
        val hit = (cache.get(code) as RelayBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayBundle> = inner.list()
}

fun newCachedRelayBundleStore(inner: RelayBundleStore, ttl: Int, clock: Clock): RelayBundleStore {
    return CachedRelayBundleStore(
        inner = inner,
        cache = newTtlCache<RelayBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay bundles. Empty fields match all. */
data class RelayBundleQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayBundle>): MutableList<RelayBundle> {
        val matched = filter(items) { candidate: RelayBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayBundles(store: RelayBundleStore, query: RelayBundleQuery): MutableList<RelayBundle> {
    return query.apply(store.list())
}
