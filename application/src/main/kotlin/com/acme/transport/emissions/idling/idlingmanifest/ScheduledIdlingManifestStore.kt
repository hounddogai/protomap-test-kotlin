package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled idling manifests by code. */
interface ScheduledIdlingManifestStore {
    fun find(code: String): ScheduledIdlingManifest?
    fun save(item: ScheduledIdlingManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledIdlingManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledIdlingManifestStore(
    var items: MutableMap<String, ScheduledIdlingManifest>,
    var order: MutableList<String>,
) : ScheduledIdlingManifestStore {
    override fun find(code: String): ScheduledIdlingManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledIdlingManifest) {
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

    override fun list(): MutableList<ScheduledIdlingManifest> {
        val out = mutableListOf<ScheduledIdlingManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledIdlingManifestStore(): ScheduledIdlingManifestStore {
    return MemoryScheduledIdlingManifestStore(
        mutableMapOf<String, ScheduledIdlingManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledIdlingManifestStore(
    var inner: ScheduledIdlingManifestStore,
    var cache: TtlCache<ScheduledIdlingManifest>,
    var hits: Int,
    var misses: Int,
) : ScheduledIdlingManifestStore {
    override fun find(code: String): ScheduledIdlingManifest? {
        val hit = (cache.get(code) as ScheduledIdlingManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledIdlingManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledIdlingManifest> = inner.list()
}

fun newCachedScheduledIdlingManifestStore(inner: ScheduledIdlingManifestStore, ttl: Int, clock: Clock): ScheduledIdlingManifestStore {
    return CachedScheduledIdlingManifestStore(
        inner = inner,
        cache = newTtlCache<ScheduledIdlingManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled idling manifests. Empty fields match all. */
data class ScheduledIdlingManifestQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledIdlingManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ScheduledIdlingManifest>): MutableList<ScheduledIdlingManifest> {
        val matched = filter(items) { candidate: ScheduledIdlingManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledIdlingManifests(store: ScheduledIdlingManifestStore, query: ScheduledIdlingManifestQuery): MutableList<ScheduledIdlingManifest> {
    return query.apply(store.list())
}
