package com.acme.transport.linehaul.trailer.trailergroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled trailer manifests by code. */
interface ScheduledTrailerManifestStore {
    fun find(code: String): ScheduledTrailerManifest?
    fun save(item: ScheduledTrailerManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTrailerManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTrailerManifestStore(
    var items: MutableMap<String, ScheduledTrailerManifest>,
    var order: MutableList<String>,
) : ScheduledTrailerManifestStore {
    override fun find(code: String): ScheduledTrailerManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTrailerManifest) {
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

    override fun list(): MutableList<ScheduledTrailerManifest> {
        val out = mutableListOf<ScheduledTrailerManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTrailerManifestStore(): ScheduledTrailerManifestStore {
    return MemoryScheduledTrailerManifestStore(
        mutableMapOf<String, ScheduledTrailerManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTrailerManifestStore(
    var inner: ScheduledTrailerManifestStore,
    var cache: TtlCache<ScheduledTrailerManifest>,
    var hits: Int,
    var misses: Int,
) : ScheduledTrailerManifestStore {
    override fun find(code: String): ScheduledTrailerManifest? {
        val hit = (cache.get(code) as ScheduledTrailerManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTrailerManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTrailerManifest> = inner.list()
}

fun newCachedScheduledTrailerManifestStore(inner: ScheduledTrailerManifestStore, ttl: Int, clock: Clock): ScheduledTrailerManifestStore {
    return CachedScheduledTrailerManifestStore(
        inner = inner,
        cache = newTtlCache<ScheduledTrailerManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled trailer manifests. Empty fields match all. */
data class ScheduledTrailerManifestQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTrailerManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ScheduledTrailerManifest>): MutableList<ScheduledTrailerManifest> {
        val matched = filter(items) { candidate: ScheduledTrailerManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTrailerManifests(store: ScheduledTrailerManifestStore, query: ScheduledTrailerManifestQuery): MutableList<ScheduledTrailerManifest> {
    return query.apply(store.list())
}
