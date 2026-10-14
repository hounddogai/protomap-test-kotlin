package com.acme.transport.network.node.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict node bundles by code. */
interface StrictNodeBundleStore {
    fun find(code: String): StrictNodeBundle?
    fun save(item: StrictNodeBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictNodeBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictNodeBundleStore(
    var items: MutableMap<String, StrictNodeBundle>,
    var order: MutableList<String>,
) : StrictNodeBundleStore {
    override fun find(code: String): StrictNodeBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictNodeBundle) {
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

    override fun list(): MutableList<StrictNodeBundle> {
        val out = mutableListOf<StrictNodeBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictNodeBundleStore(): StrictNodeBundleStore {
    return MemoryStrictNodeBundleStore(mutableMapOf<String, StrictNodeBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictNodeBundleStore(
    var inner: StrictNodeBundleStore,
    var cache: TtlCache<StrictNodeBundle>,
    var hits: Int,
    var misses: Int,
) : StrictNodeBundleStore {
    override fun find(code: String): StrictNodeBundle? {
        val hit = (cache.get(code) as StrictNodeBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictNodeBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictNodeBundle> = inner.list()
}

fun newCachedStrictNodeBundleStore(inner: StrictNodeBundleStore, ttl: Int, clock: Clock): StrictNodeBundleStore {
    return CachedStrictNodeBundleStore(
        inner = inner,
        cache = newTtlCache<StrictNodeBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict node bundles. Empty fields match all. */
data class StrictNodeBundleQuery(
    var segment: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictNodeBundle): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictNodeBundle>): MutableList<StrictNodeBundle> {
        val matched = filter(items) { candidate: StrictNodeBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictNodeBundles(store: StrictNodeBundleStore, query: StrictNodeBundleQuery): MutableList<StrictNodeBundle> {
    return query.apply(store.list())
}
