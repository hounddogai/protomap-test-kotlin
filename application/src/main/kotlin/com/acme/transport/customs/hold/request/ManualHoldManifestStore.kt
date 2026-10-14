package com.acme.transport.customs.hold.request

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual hold manifests by code. */
interface ManualHoldManifestStore {
    fun find(code: String): ManualHoldManifest?
    fun save(item: ManualHoldManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualHoldManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualHoldManifestStore(
    var items: MutableMap<String, ManualHoldManifest>,
    var order: MutableList<String>,
) : ManualHoldManifestStore {
    override fun find(code: String): ManualHoldManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualHoldManifest) {
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

    override fun list(): MutableList<ManualHoldManifest> {
        val out = mutableListOf<ManualHoldManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualHoldManifestStore(): ManualHoldManifestStore {
    return MemoryManualHoldManifestStore(mutableMapOf<String, ManualHoldManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualHoldManifestStore(
    var inner: ManualHoldManifestStore,
    var cache: TtlCache<ManualHoldManifest>,
    var hits: Int,
    var misses: Int,
) : ManualHoldManifestStore {
    override fun find(code: String): ManualHoldManifest? {
        val hit = (cache.get(code) as ManualHoldManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualHoldManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualHoldManifest> = inner.list()
}

fun newCachedManualHoldManifestStore(inner: ManualHoldManifestStore, ttl: Int, clock: Clock): ManualHoldManifestStore {
    return CachedManualHoldManifestStore(
        inner = inner,
        cache = newTtlCache<ManualHoldManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual hold manifests. Empty fields match all. */
data class ManualHoldManifestQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualHoldManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualHoldManifest>): MutableList<ManualHoldManifest> {
        val matched = filter(items) { candidate: ManualHoldManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualHoldManifests(store: ManualHoldManifestStore, query: ManualHoldManifestQuery): MutableList<ManualHoldManifest> {
    return query.apply(store.list())
}
