package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual dwell manifests by code. */
interface ManualDwellManifestStore {
    fun find(code: String): ManualDwellManifest?
    fun save(item: ManualDwellManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDwellManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDwellManifestStore(
    var items: MutableMap<String, ManualDwellManifest>,
    var order: MutableList<String>,
) : ManualDwellManifestStore {
    override fun find(code: String): ManualDwellManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDwellManifest) {
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

    override fun list(): MutableList<ManualDwellManifest> {
        val out = mutableListOf<ManualDwellManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDwellManifestStore(): ManualDwellManifestStore {
    return MemoryManualDwellManifestStore(mutableMapOf<String, ManualDwellManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDwellManifestStore(
    var inner: ManualDwellManifestStore,
    var cache: TtlCache<ManualDwellManifest>,
    var hits: Int,
    var misses: Int,
) : ManualDwellManifestStore {
    override fun find(code: String): ManualDwellManifest? {
        val hit = (cache.get(code) as ManualDwellManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDwellManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDwellManifest> = inner.list()
}

fun newCachedManualDwellManifestStore(inner: ManualDwellManifestStore, ttl: Int, clock: Clock): ManualDwellManifestStore {
    return CachedManualDwellManifestStore(
        inner = inner,
        cache = newTtlCache<ManualDwellManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual dwell manifests. Empty fields match all. */
data class ManualDwellManifestQuery(
    var source: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDwellManifest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDwellManifest>): MutableList<ManualDwellManifest> {
        val matched = filter(items) { candidate: ManualDwellManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDwellManifests(store: ManualDwellManifestStore, query: ManualDwellManifestQuery): MutableList<ManualDwellManifest> {
    return query.apply(store.list())
}
