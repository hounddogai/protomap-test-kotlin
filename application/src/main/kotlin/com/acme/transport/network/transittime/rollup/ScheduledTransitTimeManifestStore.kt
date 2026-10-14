package com.acme.transport.network.transittime.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled transit time manifests by code. */
interface ScheduledTransitTimeManifestStore {
    fun find(code: String): ScheduledTransitTimeManifest?
    fun save(item: ScheduledTransitTimeManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTransitTimeManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTransitTimeManifestStore(
    var items: MutableMap<String, ScheduledTransitTimeManifest>,
    var order: MutableList<String>,
) : ScheduledTransitTimeManifestStore {
    override fun find(code: String): ScheduledTransitTimeManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTransitTimeManifest) {
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

    override fun list(): MutableList<ScheduledTransitTimeManifest> {
        val out = mutableListOf<ScheduledTransitTimeManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTransitTimeManifestStore(): ScheduledTransitTimeManifestStore {
    return MemoryScheduledTransitTimeManifestStore(
        mutableMapOf<String, ScheduledTransitTimeManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTransitTimeManifestStore(
    var inner: ScheduledTransitTimeManifestStore,
    var cache: TtlCache<ScheduledTransitTimeManifest>,
    var hits: Int,
    var misses: Int,
) : ScheduledTransitTimeManifestStore {
    override fun find(code: String): ScheduledTransitTimeManifest? {
        val hit = (cache.get(code) as ScheduledTransitTimeManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTransitTimeManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTransitTimeManifest> = inner.list()
}

fun newCachedScheduledTransitTimeManifestStore(inner: ScheduledTransitTimeManifestStore, ttl: Int, clock: Clock): ScheduledTransitTimeManifestStore {
    return CachedScheduledTransitTimeManifestStore(
        inner = inner,
        cache = newTtlCache<ScheduledTransitTimeManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled transit time manifests. Empty fields match all. */
data class ScheduledTransitTimeManifestQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTransitTimeManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTransitTimeManifest>): MutableList<ScheduledTransitTimeManifest> {
        val matched = filter(items) { candidate: ScheduledTransitTimeManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTransitTimeManifests(store: ScheduledTransitTimeManifestStore, query: ScheduledTransitTimeManifestQuery): MutableList<ScheduledTransitTimeManifest> {
    return query.apply(store.list())
}
