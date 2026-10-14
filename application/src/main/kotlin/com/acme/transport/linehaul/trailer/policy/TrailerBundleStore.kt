package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer bundles by code. */
interface TrailerBundleStore {
    fun find(code: String): TrailerBundle?
    fun save(item: TrailerBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerBundleStore(
    var items: MutableMap<String, TrailerBundle>,
    var order: MutableList<String>,
) : TrailerBundleStore {
    override fun find(code: String): TrailerBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerBundle) {
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

    override fun list(): MutableList<TrailerBundle> {
        val out = mutableListOf<TrailerBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerBundleStore(): TrailerBundleStore {
    return MemoryTrailerBundleStore(mutableMapOf<String, TrailerBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerBundleStore(
    var inner: TrailerBundleStore,
    var cache: TtlCache<TrailerBundle>,
    var hits: Int,
    var misses: Int,
) : TrailerBundleStore {
    override fun find(code: String): TrailerBundle? {
        val hit = (cache.get(code) as TrailerBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerBundle> = inner.list()
}

fun newCachedTrailerBundleStore(inner: TrailerBundleStore, ttl: Int, clock: Clock): TrailerBundleStore {
    return CachedTrailerBundleStore(
        inner = inner,
        cache = newTtlCache<TrailerBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer bundles. Empty fields match all. */
data class TrailerBundleQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerBundle>): MutableList<TrailerBundle> {
        val matched = filter(items) { candidate: TrailerBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerBundles(store: TrailerBundleStore, query: TrailerBundleQuery): MutableList<TrailerBundle> {
    return query.apply(store.list())
}
