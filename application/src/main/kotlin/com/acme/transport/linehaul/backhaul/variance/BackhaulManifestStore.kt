package com.acme.transport.linehaul.backhaul.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul manifests by code. */
interface BackhaulManifestStore {
    fun find(code: String): BackhaulManifest?
    fun save(item: BackhaulManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulManifestStore(
    var items: MutableMap<String, BackhaulManifest>,
    var order: MutableList<String>,
) : BackhaulManifestStore {
    override fun find(code: String): BackhaulManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulManifest) {
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

    override fun list(): MutableList<BackhaulManifest> {
        val out = mutableListOf<BackhaulManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulManifestStore(): BackhaulManifestStore {
    return MemoryBackhaulManifestStore(mutableMapOf<String, BackhaulManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulManifestStore(
    var inner: BackhaulManifestStore,
    var cache: TtlCache<BackhaulManifest>,
    var hits: Int,
    var misses: Int,
) : BackhaulManifestStore {
    override fun find(code: String): BackhaulManifest? {
        val hit = (cache.get(code) as BackhaulManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulManifest> = inner.list()
}

fun newCachedBackhaulManifestStore(inner: BackhaulManifestStore, ttl: Int, clock: Clock): BackhaulManifestStore {
    return CachedBackhaulManifestStore(
        inner = inner,
        cache = newTtlCache<BackhaulManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul manifests. Empty fields match all. */
data class BackhaulManifestQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulManifest>): MutableList<BackhaulManifest> {
        val matched = filter(items) { candidate: BackhaulManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulManifests(store: BackhaulManifestStore, query: BackhaulManifestQuery): MutableList<BackhaulManifest> {
    return query.apply(store.list())
}
