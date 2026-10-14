package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual scorecard bundles by code. */
interface ManualScorecardBundleStore {
    fun find(code: String): ManualScorecardBundle?
    fun save(item: ManualScorecardBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualScorecardBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualScorecardBundleStore(
    var items: MutableMap<String, ManualScorecardBundle>,
    var order: MutableList<String>,
) : ManualScorecardBundleStore {
    override fun find(code: String): ManualScorecardBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualScorecardBundle) {
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

    override fun list(): MutableList<ManualScorecardBundle> {
        val out = mutableListOf<ManualScorecardBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualScorecardBundleStore(): ManualScorecardBundleStore {
    return MemoryManualScorecardBundleStore(
        mutableMapOf<String, ManualScorecardBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualScorecardBundleStore(
    var inner: ManualScorecardBundleStore,
    var cache: TtlCache<ManualScorecardBundle>,
    var hits: Int,
    var misses: Int,
) : ManualScorecardBundleStore {
    override fun find(code: String): ManualScorecardBundle? {
        val hit = (cache.get(code) as ManualScorecardBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualScorecardBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualScorecardBundle> = inner.list()
}

fun newCachedManualScorecardBundleStore(inner: ManualScorecardBundleStore, ttl: Int, clock: Clock): ManualScorecardBundleStore {
    return CachedManualScorecardBundleStore(
        inner = inner,
        cache = newTtlCache<ManualScorecardBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual scorecard bundles. Empty fields match all. */
data class ManualScorecardBundleQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualScorecardBundle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualScorecardBundle>): MutableList<ManualScorecardBundle> {
        val matched = filter(items) { candidate: ManualScorecardBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualScorecardBundles(store: ManualScorecardBundleStore, query: ManualScorecardBundleQuery): MutableList<ManualScorecardBundle> {
    return query.apply(store.list())
}
