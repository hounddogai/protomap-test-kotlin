package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt manifests by code. */
interface AttemptManifestStore {
    fun find(code: String): AttemptManifest?
    fun save(item: AttemptManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptManifestStore(
    var items: MutableMap<String, AttemptManifest>,
    var order: MutableList<String>,
) : AttemptManifestStore {
    override fun find(code: String): AttemptManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptManifest) {
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

    override fun list(): MutableList<AttemptManifest> {
        val out = mutableListOf<AttemptManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptManifestStore(): AttemptManifestStore {
    return MemoryAttemptManifestStore(mutableMapOf<String, AttemptManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptManifestStore(
    var inner: AttemptManifestStore,
    var cache: TtlCache<AttemptManifest>,
    var hits: Int,
    var misses: Int,
) : AttemptManifestStore {
    override fun find(code: String): AttemptManifest? {
        val hit = (cache.get(code) as AttemptManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptManifest> = inner.list()
}

fun newCachedAttemptManifestStore(inner: AttemptManifestStore, ttl: Int, clock: Clock): AttemptManifestStore {
    return CachedAttemptManifestStore(
        inner = inner,
        cache = newTtlCache<AttemptManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt manifests. Empty fields match all. */
data class AttemptManifestQuery(
    var unit: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptManifest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<AttemptManifest>): MutableList<AttemptManifest> {
        val matched = filter(items) { candidate: AttemptManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptManifests(store: AttemptManifestStore, query: AttemptManifestQuery): MutableList<AttemptManifest> {
    return query.apply(store.list())
}
