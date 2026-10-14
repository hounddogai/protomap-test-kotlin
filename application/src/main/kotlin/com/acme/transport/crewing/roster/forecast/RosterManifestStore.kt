package com.acme.transport.crewing.roster.forecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster manifests by code. */
interface RosterManifestStore {
    fun find(code: String): RosterManifest?
    fun save(item: RosterManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterManifestStore(
    var items: MutableMap<String, RosterManifest>,
    var order: MutableList<String>,
) : RosterManifestStore {
    override fun find(code: String): RosterManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterManifest) {
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

    override fun list(): MutableList<RosterManifest> {
        val out = mutableListOf<RosterManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterManifestStore(): RosterManifestStore {
    return MemoryRosterManifestStore(mutableMapOf<String, RosterManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterManifestStore(
    var inner: RosterManifestStore,
    var cache: TtlCache<RosterManifest>,
    var hits: Int,
    var misses: Int,
) : RosterManifestStore {
    override fun find(code: String): RosterManifest? {
        val hit = (cache.get(code) as RosterManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterManifest> = inner.list()
}

fun newCachedRosterManifestStore(inner: RosterManifestStore, ttl: Int, clock: Clock): RosterManifestStore {
    return CachedRosterManifestStore(
        inner = inner,
        cache = newTtlCache<RosterManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster manifests. Empty fields match all. */
data class RosterManifestQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterManifest>): MutableList<RosterManifest> {
        val matched = filter(items) { candidate: RosterManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterManifests(store: RosterManifestStore, query: RosterManifestQuery): MutableList<RosterManifest> {
    return query.apply(store.list())
}
