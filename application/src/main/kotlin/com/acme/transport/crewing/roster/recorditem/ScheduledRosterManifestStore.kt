package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled roster manifests by code. */
interface ScheduledRosterManifestStore {
    fun find(code: String): ScheduledRosterManifest?
    fun save(item: ScheduledRosterManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRosterManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRosterManifestStore(
    var items: MutableMap<String, ScheduledRosterManifest>,
    var order: MutableList<String>,
) : ScheduledRosterManifestStore {
    override fun find(code: String): ScheduledRosterManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRosterManifest) {
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

    override fun list(): MutableList<ScheduledRosterManifest> {
        val out = mutableListOf<ScheduledRosterManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRosterManifestStore(): ScheduledRosterManifestStore {
    return MemoryScheduledRosterManifestStore(
        mutableMapOf<String, ScheduledRosterManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRosterManifestStore(
    var inner: ScheduledRosterManifestStore,
    var cache: TtlCache<ScheduledRosterManifest>,
    var hits: Int,
    var misses: Int,
) : ScheduledRosterManifestStore {
    override fun find(code: String): ScheduledRosterManifest? {
        val hit = (cache.get(code) as ScheduledRosterManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRosterManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRosterManifest> = inner.list()
}

fun newCachedScheduledRosterManifestStore(inner: ScheduledRosterManifestStore, ttl: Int, clock: Clock): ScheduledRosterManifestStore {
    return CachedScheduledRosterManifestStore(
        inner = inner,
        cache = newTtlCache<ScheduledRosterManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled roster manifests. Empty fields match all. */
data class ScheduledRosterManifestQuery(
    var channel: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRosterManifest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRosterManifest>): MutableList<ScheduledRosterManifest> {
        val matched = filter(items) { candidate: ScheduledRosterManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRosterManifests(store: ScheduledRosterManifestStore, query: ScheduledRosterManifestQuery): MutableList<ScheduledRosterManifest> {
    return query.apply(store.list())
}
