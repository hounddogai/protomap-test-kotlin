package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling tender manifests by code. */
interface RollingTenderManifestStore {
    fun find(code: String): RollingTenderManifest?
    fun save(item: RollingTenderManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTenderManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTenderManifestStore(
    var items: MutableMap<String, RollingTenderManifest>,
    var order: MutableList<String>,
) : RollingTenderManifestStore {
    override fun find(code: String): RollingTenderManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTenderManifest) {
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

    override fun list(): MutableList<RollingTenderManifest> {
        val out = mutableListOf<RollingTenderManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTenderManifestStore(): RollingTenderManifestStore {
    return MemoryRollingTenderManifestStore(
        mutableMapOf<String, RollingTenderManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTenderManifestStore(
    var inner: RollingTenderManifestStore,
    var cache: TtlCache<RollingTenderManifest>,
    var hits: Int,
    var misses: Int,
) : RollingTenderManifestStore {
    override fun find(code: String): RollingTenderManifest? {
        val hit = (cache.get(code) as RollingTenderManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTenderManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTenderManifest> = inner.list()
}

fun newCachedRollingTenderManifestStore(inner: RollingTenderManifestStore, ttl: Int, clock: Clock): RollingTenderManifestStore {
    return CachedRollingTenderManifestStore(
        inner = inner,
        cache = newTtlCache<RollingTenderManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling tender manifests. Empty fields match all. */
data class RollingTenderManifestQuery(
    var channel: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTenderManifest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTenderManifest>): MutableList<RollingTenderManifest> {
        val matched = filter(items) { candidate: RollingTenderManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTenderManifests(store: RollingTenderManifestStore, query: RollingTenderManifestQuery): MutableList<RollingTenderManifest> {
    return query.apply(store.list())
}
