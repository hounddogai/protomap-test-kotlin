package com.acme.transport.network.node.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending node bundles by code. */
interface PendingNodeBundleStore {
    fun find(code: String): PendingNodeBundle?
    fun save(item: PendingNodeBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNodeBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNodeBundleStore(
    var items: MutableMap<String, PendingNodeBundle>,
    var order: MutableList<String>,
) : PendingNodeBundleStore {
    override fun find(code: String): PendingNodeBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNodeBundle) {
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

    override fun list(): MutableList<PendingNodeBundle> {
        val out = mutableListOf<PendingNodeBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNodeBundleStore(): PendingNodeBundleStore {
    return MemoryPendingNodeBundleStore(mutableMapOf<String, PendingNodeBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNodeBundleStore(
    var inner: PendingNodeBundleStore,
    var cache: TtlCache<PendingNodeBundle>,
    var hits: Int,
    var misses: Int,
) : PendingNodeBundleStore {
    override fun find(code: String): PendingNodeBundle? {
        val hit = (cache.get(code) as PendingNodeBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNodeBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNodeBundle> = inner.list()
}

fun newCachedPendingNodeBundleStore(inner: PendingNodeBundleStore, ttl: Int, clock: Clock): PendingNodeBundleStore {
    return CachedPendingNodeBundleStore(
        inner = inner,
        cache = newTtlCache<PendingNodeBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending node bundles. Empty fields match all. */
data class PendingNodeBundleQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNodeBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingNodeBundle>): MutableList<PendingNodeBundle> {
        val matched = filter(items) { candidate: PendingNodeBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNodeBundles(store: PendingNodeBundleStore, query: PendingNodeBundleQuery): MutableList<PendingNodeBundle> {
    return query.apply(store.list())
}
