package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard bundles by code. */
interface ScorecardBundleStore {
    fun find(code: String): ScorecardBundle?
    fun save(item: ScorecardBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardBundleStore(
    var items: MutableMap<String, ScorecardBundle>,
    var order: MutableList<String>,
) : ScorecardBundleStore {
    override fun find(code: String): ScorecardBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardBundle) {
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

    override fun list(): MutableList<ScorecardBundle> {
        val out = mutableListOf<ScorecardBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardBundleStore(): ScorecardBundleStore {
    return MemoryScorecardBundleStore(mutableMapOf<String, ScorecardBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardBundleStore(
    var inner: ScorecardBundleStore,
    var cache: TtlCache<ScorecardBundle>,
    var hits: Int,
    var misses: Int,
) : ScorecardBundleStore {
    override fun find(code: String): ScorecardBundle? {
        val hit = (cache.get(code) as ScorecardBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardBundle> = inner.list()
}

fun newCachedScorecardBundleStore(inner: ScorecardBundleStore, ttl: Int, clock: Clock): ScorecardBundleStore {
    return CachedScorecardBundleStore(
        inner = inner,
        cache = newTtlCache<ScorecardBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard bundles. Empty fields match all. */
data class ScorecardBundleQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardBundle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ScorecardBundle>): MutableList<ScorecardBundle> {
        val matched = filter(items) { candidate: ScorecardBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardBundles(store: ScorecardBundleStore, query: ScorecardBundleQuery): MutableList<ScorecardBundle> {
    return query.apply(store.list())
}
