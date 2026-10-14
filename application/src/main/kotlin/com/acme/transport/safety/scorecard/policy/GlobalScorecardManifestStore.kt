package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global scorecard manifests by code. */
interface GlobalScorecardManifestStore {
    fun find(code: String): GlobalScorecardManifest?
    fun save(item: GlobalScorecardManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalScorecardManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalScorecardManifestStore(
    var items: MutableMap<String, GlobalScorecardManifest>,
    var order: MutableList<String>,
) : GlobalScorecardManifestStore {
    override fun find(code: String): GlobalScorecardManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalScorecardManifest) {
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

    override fun list(): MutableList<GlobalScorecardManifest> {
        val out = mutableListOf<GlobalScorecardManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalScorecardManifestStore(): GlobalScorecardManifestStore {
    return MemoryGlobalScorecardManifestStore(
        mutableMapOf<String, GlobalScorecardManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalScorecardManifestStore(
    var inner: GlobalScorecardManifestStore,
    var cache: TtlCache<GlobalScorecardManifest>,
    var hits: Int,
    var misses: Int,
) : GlobalScorecardManifestStore {
    override fun find(code: String): GlobalScorecardManifest? {
        val hit = (cache.get(code) as GlobalScorecardManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalScorecardManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalScorecardManifest> = inner.list()
}

fun newCachedGlobalScorecardManifestStore(inner: GlobalScorecardManifestStore, ttl: Int, clock: Clock): GlobalScorecardManifestStore {
    return CachedGlobalScorecardManifestStore(
        inner = inner,
        cache = newTtlCache<GlobalScorecardManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global scorecard manifests. Empty fields match all. */
data class GlobalScorecardManifestQuery(
    var channel: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalScorecardManifest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalScorecardManifest>): MutableList<GlobalScorecardManifest> {
        val matched = filter(items) { candidate: GlobalScorecardManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalScorecardManifests(store: GlobalScorecardManifestStore, query: GlobalScorecardManifestQuery): MutableList<GlobalScorecardManifest> {
    return query.apply(store.list())
}
