package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient node manifests by code. */
interface LenientNodeManifestStore {
    fun find(code: String): LenientNodeManifest?
    fun save(item: LenientNodeManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientNodeManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientNodeManifestStore(
    var items: MutableMap<String, LenientNodeManifest>,
    var order: MutableList<String>,
) : LenientNodeManifestStore {
    override fun find(code: String): LenientNodeManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientNodeManifest) {
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

    override fun list(): MutableList<LenientNodeManifest> {
        val out = mutableListOf<LenientNodeManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientNodeManifestStore(): LenientNodeManifestStore {
    return MemoryLenientNodeManifestStore(mutableMapOf<String, LenientNodeManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientNodeManifestStore(
    var inner: LenientNodeManifestStore,
    var cache: TtlCache<LenientNodeManifest>,
    var hits: Int,
    var misses: Int,
) : LenientNodeManifestStore {
    override fun find(code: String): LenientNodeManifest? {
        val hit = (cache.get(code) as LenientNodeManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientNodeManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientNodeManifest> = inner.list()
}

fun newCachedLenientNodeManifestStore(inner: LenientNodeManifestStore, ttl: Int, clock: Clock): LenientNodeManifestStore {
    return CachedLenientNodeManifestStore(
        inner = inner,
        cache = newTtlCache<LenientNodeManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient node manifests. Empty fields match all. */
data class LenientNodeManifestQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientNodeManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LenientNodeManifest>): MutableList<LenientNodeManifest> {
        val matched = filter(items) { candidate: LenientNodeManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientNodeManifests(store: LenientNodeManifestStore, query: LenientNodeManifestQuery): MutableList<LenientNodeManifest> {
    return query.apply(store.list())
}
