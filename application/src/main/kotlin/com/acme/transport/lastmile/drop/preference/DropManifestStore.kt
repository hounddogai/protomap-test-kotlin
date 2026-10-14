package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop manifests by code. */
interface DropManifestStore {
    fun find(code: String): DropManifest?
    fun save(item: DropManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropManifestStore(
    var items: MutableMap<String, DropManifest>,
    var order: MutableList<String>,
) : DropManifestStore {
    override fun find(code: String): DropManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropManifest) {
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

    override fun list(): MutableList<DropManifest> {
        val out = mutableListOf<DropManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropManifestStore(): DropManifestStore {
    return MemoryDropManifestStore(mutableMapOf<String, DropManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropManifestStore(
    var inner: DropManifestStore,
    var cache: TtlCache<DropManifest>,
    var hits: Int,
    var misses: Int,
) : DropManifestStore {
    override fun find(code: String): DropManifest? {
        val hit = (cache.get(code) as DropManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropManifest> = inner.list()
}

fun newCachedDropManifestStore(inner: DropManifestStore, ttl: Int, clock: Clock): DropManifestStore {
    return CachedDropManifestStore(
        inner = inner,
        cache = newTtlCache<DropManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop manifests. Empty fields match all. */
data class DropManifestQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropManifest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropManifest>): MutableList<DropManifest> {
        val matched = filter(items) { candidate: DropManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropManifests(store: DropManifestStore, query: DropManifestQuery): MutableList<DropManifest> {
    return query.apply(store.list())
}
