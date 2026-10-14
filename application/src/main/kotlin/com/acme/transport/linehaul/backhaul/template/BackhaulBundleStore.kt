package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul bundles by code. */
interface BackhaulBundleStore {
    fun find(code: String): BackhaulBundle?
    fun save(item: BackhaulBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulBundleStore(
    var items: MutableMap<String, BackhaulBundle>,
    var order: MutableList<String>,
) : BackhaulBundleStore {
    override fun find(code: String): BackhaulBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulBundle) {
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

    override fun list(): MutableList<BackhaulBundle> {
        val out = mutableListOf<BackhaulBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulBundleStore(): BackhaulBundleStore {
    return MemoryBackhaulBundleStore(mutableMapOf<String, BackhaulBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulBundleStore(
    var inner: BackhaulBundleStore,
    var cache: TtlCache<BackhaulBundle>,
    var hits: Int,
    var misses: Int,
) : BackhaulBundleStore {
    override fun find(code: String): BackhaulBundle? {
        val hit = (cache.get(code) as BackhaulBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulBundle> = inner.list()
}

fun newCachedBackhaulBundleStore(inner: BackhaulBundleStore, ttl: Int, clock: Clock): BackhaulBundleStore {
    return CachedBackhaulBundleStore(
        inner = inner,
        cache = newTtlCache<BackhaulBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul bundles. Empty fields match all. */
data class BackhaulBundleQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulBundle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulBundle>): MutableList<BackhaulBundle> {
        val matched = filter(items) { candidate: BackhaulBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulBundles(store: BackhaulBundleStore, query: BackhaulBundleQuery): MutableList<BackhaulBundle> {
    return query.apply(store.list())
}
