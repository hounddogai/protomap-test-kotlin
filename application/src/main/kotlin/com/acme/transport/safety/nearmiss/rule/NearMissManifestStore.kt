package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss manifests by code. */
interface NearMissManifestStore {
    fun find(code: String): NearMissManifest?
    fun save(item: NearMissManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissManifestStore(
    var items: MutableMap<String, NearMissManifest>,
    var order: MutableList<String>,
) : NearMissManifestStore {
    override fun find(code: String): NearMissManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissManifest) {
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

    override fun list(): MutableList<NearMissManifest> {
        val out = mutableListOf<NearMissManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissManifestStore(): NearMissManifestStore {
    return MemoryNearMissManifestStore(mutableMapOf<String, NearMissManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissManifestStore(
    var inner: NearMissManifestStore,
    var cache: TtlCache<NearMissManifest>,
    var hits: Int,
    var misses: Int,
) : NearMissManifestStore {
    override fun find(code: String): NearMissManifest? {
        val hit = (cache.get(code) as NearMissManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissManifest> = inner.list()
}

fun newCachedNearMissManifestStore(inner: NearMissManifestStore, ttl: Int, clock: Clock): NearMissManifestStore {
    return CachedNearMissManifestStore(
        inner = inner,
        cache = newTtlCache<NearMissManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss manifests. Empty fields match all. */
data class NearMissManifestQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissManifest>): MutableList<NearMissManifest> {
        val matched = filter(items) { candidate: NearMissManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissManifests(store: NearMissManifestStore, query: NearMissManifestQuery): MutableList<NearMissManifest> {
    return query.apply(store.list())
}
