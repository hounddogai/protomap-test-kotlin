package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy sortation manifests by code. */
interface LegacySortationManifestStore {
    fun find(code: String): LegacySortationManifest?
    fun save(item: LegacySortationManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacySortationManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacySortationManifestStore(
    var items: MutableMap<String, LegacySortationManifest>,
    var order: MutableList<String>,
) : LegacySortationManifestStore {
    override fun find(code: String): LegacySortationManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacySortationManifest) {
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

    override fun list(): MutableList<LegacySortationManifest> {
        val out = mutableListOf<LegacySortationManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacySortationManifestStore(): LegacySortationManifestStore {
    return MemoryLegacySortationManifestStore(
        mutableMapOf<String, LegacySortationManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacySortationManifestStore(
    var inner: LegacySortationManifestStore,
    var cache: TtlCache<LegacySortationManifest>,
    var hits: Int,
    var misses: Int,
) : LegacySortationManifestStore {
    override fun find(code: String): LegacySortationManifest? {
        val hit = (cache.get(code) as LegacySortationManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacySortationManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacySortationManifest> = inner.list()
}

fun newCachedLegacySortationManifestStore(inner: LegacySortationManifestStore, ttl: Int, clock: Clock): LegacySortationManifestStore {
    return CachedLegacySortationManifestStore(
        inner = inner,
        cache = newTtlCache<LegacySortationManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy sortation manifests. Empty fields match all. */
data class LegacySortationManifestQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacySortationManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacySortationManifest>): MutableList<LegacySortationManifest> {
        val matched = filter(items) { candidate: LegacySortationManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacySortationManifests(store: LegacySortationManifestStore, query: LegacySortationManifestQuery): MutableList<LegacySortationManifest> {
    return query.apply(store.list())
}
