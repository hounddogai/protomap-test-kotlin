package com.acme.transport.emissions.idling.bundle

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary idling bundles by code. */
interface PrimaryIdlingBundleStore {
    fun find(code: String): PrimaryIdlingBundle?
    fun save(item: PrimaryIdlingBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryIdlingBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryIdlingBundleStore(
    var items: MutableMap<String, PrimaryIdlingBundle>,
    var order: MutableList<String>,
) : PrimaryIdlingBundleStore {
    override fun find(code: String): PrimaryIdlingBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryIdlingBundle) {
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

    override fun list(): MutableList<PrimaryIdlingBundle> {
        val out = mutableListOf<PrimaryIdlingBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryIdlingBundleStore(): PrimaryIdlingBundleStore {
    return MemoryPrimaryIdlingBundleStore(mutableMapOf<String, PrimaryIdlingBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryIdlingBundleStore(
    var inner: PrimaryIdlingBundleStore,
    var cache: TtlCache<PrimaryIdlingBundle>,
    var hits: Int,
    var misses: Int,
) : PrimaryIdlingBundleStore {
    override fun find(code: String): PrimaryIdlingBundle? {
        val hit = (cache.get(code) as PrimaryIdlingBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryIdlingBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryIdlingBundle> = inner.list()
}

fun newCachedPrimaryIdlingBundleStore(inner: PrimaryIdlingBundleStore, ttl: Int, clock: Clock): PrimaryIdlingBundleStore {
    return CachedPrimaryIdlingBundleStore(
        inner = inner,
        cache = newTtlCache<PrimaryIdlingBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary idling bundles. Empty fields match all. */
data class PrimaryIdlingBundleQuery(
    var category: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryIdlingBundle): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryIdlingBundle>): MutableList<PrimaryIdlingBundle> {
        val matched = filter(items) { candidate: PrimaryIdlingBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryIdlingBundles(store: PrimaryIdlingBundleStore, query: PrimaryIdlingBundleQuery): MutableList<PrimaryIdlingBundle> {
    return query.apply(store.list())
}
