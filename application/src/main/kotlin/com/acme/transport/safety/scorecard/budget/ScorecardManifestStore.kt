package com.acme.transport.safety.scorecard.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard manifests by code. */
interface ScorecardManifestStore {
    fun find(code: String): ScorecardManifest?
    fun save(item: ScorecardManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardManifestStore(
    var items: MutableMap<String, ScorecardManifest>,
    var order: MutableList<String>,
) : ScorecardManifestStore {
    override fun find(code: String): ScorecardManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardManifest) {
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

    override fun list(): MutableList<ScorecardManifest> {
        val out = mutableListOf<ScorecardManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardManifestStore(): ScorecardManifestStore {
    return MemoryScorecardManifestStore(mutableMapOf<String, ScorecardManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardManifestStore(
    var inner: ScorecardManifestStore,
    var cache: TtlCache<ScorecardManifest>,
    var hits: Int,
    var misses: Int,
) : ScorecardManifestStore {
    override fun find(code: String): ScorecardManifest? {
        val hit = (cache.get(code) as ScorecardManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardManifest> = inner.list()
}

fun newCachedScorecardManifestStore(inner: ScorecardManifestStore, ttl: Int, clock: Clock): ScorecardManifestStore {
    return CachedScorecardManifestStore(
        inner = inner,
        cache = newTtlCache<ScorecardManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard manifests. Empty fields match all. */
data class ScorecardManifestQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ScorecardManifest>): MutableList<ScorecardManifest> {
        val matched = filter(items) { candidate: ScorecardManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardManifests(store: ScorecardManifestStore, query: ScorecardManifestQuery): MutableList<ScorecardManifest> {
    return query.apply(store.list())
}
