package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending inspection bundles by code. */
interface PendingInspectionBundleStore {
    fun find(code: String): PendingInspectionBundle?
    fun save(item: PendingInspectionBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingInspectionBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingInspectionBundleStore(
    var items: MutableMap<String, PendingInspectionBundle>,
    var order: MutableList<String>,
) : PendingInspectionBundleStore {
    override fun find(code: String): PendingInspectionBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingInspectionBundle) {
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

    override fun list(): MutableList<PendingInspectionBundle> {
        val out = mutableListOf<PendingInspectionBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingInspectionBundleStore(): PendingInspectionBundleStore {
    return MemoryPendingInspectionBundleStore(
        mutableMapOf<String, PendingInspectionBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingInspectionBundleStore(
    var inner: PendingInspectionBundleStore,
    var cache: TtlCache<PendingInspectionBundle>,
    var hits: Int,
    var misses: Int,
) : PendingInspectionBundleStore {
    override fun find(code: String): PendingInspectionBundle? {
        val hit = (cache.get(code) as PendingInspectionBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingInspectionBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingInspectionBundle> = inner.list()
}

fun newCachedPendingInspectionBundleStore(inner: PendingInspectionBundleStore, ttl: Int, clock: Clock): PendingInspectionBundleStore {
    return CachedPendingInspectionBundleStore(
        inner = inner,
        cache = newTtlCache<PendingInspectionBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending inspection bundles. Empty fields match all. */
data class PendingInspectionBundleQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingInspectionBundle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingInspectionBundle>): MutableList<PendingInspectionBundle> {
        val matched = filter(items) { candidate: PendingInspectionBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingInspectionBundles(store: PendingInspectionBundleStore, query: PendingInspectionBundleQuery): MutableList<PendingInspectionBundle> {
    return query.apply(store.list())
}
