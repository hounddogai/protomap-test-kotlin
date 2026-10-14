package com.acme.transport.safety.scorecard.scorecardbundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary scorecard bundles by code. */
interface PrimaryScorecardBundleStore {
    fun find(code: String): PrimaryScorecardBundle?
    fun save(item: PrimaryScorecardBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryScorecardBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryScorecardBundleStore(
    var items: MutableMap<String, PrimaryScorecardBundle>,
    var order: MutableList<String>,
) : PrimaryScorecardBundleStore {
    override fun find(code: String): PrimaryScorecardBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryScorecardBundle) {
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

    override fun list(): MutableList<PrimaryScorecardBundle> {
        val out = mutableListOf<PrimaryScorecardBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryScorecardBundleStore(): PrimaryScorecardBundleStore {
    return MemoryPrimaryScorecardBundleStore(
        mutableMapOf<String, PrimaryScorecardBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryScorecardBundleStore(
    var inner: PrimaryScorecardBundleStore,
    var cache: TtlCache<PrimaryScorecardBundle>,
    var hits: Int,
    var misses: Int,
) : PrimaryScorecardBundleStore {
    override fun find(code: String): PrimaryScorecardBundle? {
        val hit = (cache.get(code) as PrimaryScorecardBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryScorecardBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryScorecardBundle> = inner.list()
}

fun newCachedPrimaryScorecardBundleStore(inner: PrimaryScorecardBundleStore, ttl: Int, clock: Clock): PrimaryScorecardBundleStore {
    return CachedPrimaryScorecardBundleStore(
        inner = inner,
        cache = newTtlCache<PrimaryScorecardBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary scorecard bundles. Empty fields match all. */
data class PrimaryScorecardBundleQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryScorecardBundle): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryScorecardBundle>): MutableList<PrimaryScorecardBundle> {
        val matched = filter(items) { candidate: PrimaryScorecardBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryScorecardBundles(store: PrimaryScorecardBundleStore, query: PrimaryScorecardBundleQuery): MutableList<PrimaryScorecardBundle> {
    return query.apply(store.list())
}
