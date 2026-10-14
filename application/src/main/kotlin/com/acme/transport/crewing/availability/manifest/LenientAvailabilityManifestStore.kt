package com.acme.transport.crewing.availability.manifest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient availability manifests by code. */
interface LenientAvailabilityManifestStore {
    fun find(code: String): LenientAvailabilityManifest?
    fun save(item: LenientAvailabilityManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAvailabilityManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAvailabilityManifestStore(
    var items: MutableMap<String, LenientAvailabilityManifest>,
    var order: MutableList<String>,
) : LenientAvailabilityManifestStore {
    override fun find(code: String): LenientAvailabilityManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAvailabilityManifest) {
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

    override fun list(): MutableList<LenientAvailabilityManifest> {
        val out = mutableListOf<LenientAvailabilityManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAvailabilityManifestStore(): LenientAvailabilityManifestStore {
    return MemoryLenientAvailabilityManifestStore(
        mutableMapOf<String, LenientAvailabilityManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAvailabilityManifestStore(
    var inner: LenientAvailabilityManifestStore,
    var cache: TtlCache<LenientAvailabilityManifest>,
    var hits: Int,
    var misses: Int,
) : LenientAvailabilityManifestStore {
    override fun find(code: String): LenientAvailabilityManifest? {
        val hit = (cache.get(code) as LenientAvailabilityManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAvailabilityManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAvailabilityManifest> = inner.list()
}

fun newCachedLenientAvailabilityManifestStore(inner: LenientAvailabilityManifestStore, ttl: Int, clock: Clock): LenientAvailabilityManifestStore {
    return CachedLenientAvailabilityManifestStore(
        inner = inner,
        cache = newTtlCache<LenientAvailabilityManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient availability manifests. Empty fields match all. */
data class LenientAvailabilityManifestQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAvailabilityManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientAvailabilityManifest>): MutableList<LenientAvailabilityManifest> {
        val matched = filter(items) { candidate: LenientAvailabilityManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAvailabilityManifests(store: LenientAvailabilityManifestStore, query: LenientAvailabilityManifestQuery): MutableList<LenientAvailabilityManifest> {
    return query.apply(store.list())
}
