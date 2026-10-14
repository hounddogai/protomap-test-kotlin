package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending qualification bundles by code. */
interface PendingQualificationBundleStore {
    fun find(code: String): PendingQualificationBundle?
    fun save(item: PendingQualificationBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingQualificationBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingQualificationBundleStore(
    var items: MutableMap<String, PendingQualificationBundle>,
    var order: MutableList<String>,
) : PendingQualificationBundleStore {
    override fun find(code: String): PendingQualificationBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingQualificationBundle) {
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

    override fun list(): MutableList<PendingQualificationBundle> {
        val out = mutableListOf<PendingQualificationBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingQualificationBundleStore(): PendingQualificationBundleStore {
    return MemoryPendingQualificationBundleStore(
        mutableMapOf<String, PendingQualificationBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingQualificationBundleStore(
    var inner: PendingQualificationBundleStore,
    var cache: TtlCache<PendingQualificationBundle>,
    var hits: Int,
    var misses: Int,
) : PendingQualificationBundleStore {
    override fun find(code: String): PendingQualificationBundle? {
        val hit = (cache.get(code) as PendingQualificationBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingQualificationBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingQualificationBundle> = inner.list()
}

fun newCachedPendingQualificationBundleStore(inner: PendingQualificationBundleStore, ttl: Int, clock: Clock): PendingQualificationBundleStore {
    return CachedPendingQualificationBundleStore(
        inner = inner,
        cache = newTtlCache<PendingQualificationBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending qualification bundles. Empty fields match all. */
data class PendingQualificationBundleQuery(
    var source: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingQualificationBundle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingQualificationBundle>): MutableList<PendingQualificationBundle> {
        val matched = filter(items) { candidate: PendingQualificationBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingQualificationBundles(store: PendingQualificationBundleStore, query: PendingQualificationBundleQuery): MutableList<PendingQualificationBundle> {
    return query.apply(store.list())
}
