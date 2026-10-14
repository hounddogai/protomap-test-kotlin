package com.acme.transport.fleetops.defect.mapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary defect bundles by code. */
interface PrimaryDefectBundleStore {
    fun find(code: String): PrimaryDefectBundle?
    fun save(item: PrimaryDefectBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDefectBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDefectBundleStore(
    var items: MutableMap<String, PrimaryDefectBundle>,
    var order: MutableList<String>,
) : PrimaryDefectBundleStore {
    override fun find(code: String): PrimaryDefectBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDefectBundle) {
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

    override fun list(): MutableList<PrimaryDefectBundle> {
        val out = mutableListOf<PrimaryDefectBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDefectBundleStore(): PrimaryDefectBundleStore {
    return MemoryPrimaryDefectBundleStore(mutableMapOf<String, PrimaryDefectBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDefectBundleStore(
    var inner: PrimaryDefectBundleStore,
    var cache: TtlCache<PrimaryDefectBundle>,
    var hits: Int,
    var misses: Int,
) : PrimaryDefectBundleStore {
    override fun find(code: String): PrimaryDefectBundle? {
        val hit = (cache.get(code) as PrimaryDefectBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDefectBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDefectBundle> = inner.list()
}

fun newCachedPrimaryDefectBundleStore(inner: PrimaryDefectBundleStore, ttl: Int, clock: Clock): PrimaryDefectBundleStore {
    return CachedPrimaryDefectBundleStore(
        inner = inner,
        cache = newTtlCache<PrimaryDefectBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary defect bundles. Empty fields match all. */
data class PrimaryDefectBundleQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDefectBundle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryDefectBundle>): MutableList<PrimaryDefectBundle> {
        val matched = filter(items) { candidate: PrimaryDefectBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDefectBundles(store: PrimaryDefectBundleStore, query: PrimaryDefectBundleQuery): MutableList<PrimaryDefectBundle> {
    return query.apply(store.list())
}
