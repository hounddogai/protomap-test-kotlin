package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient cutoff manifests by code. */
interface LenientCutoffManifestStore {
    fun find(code: String): LenientCutoffManifest?
    fun save(item: LenientCutoffManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCutoffManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCutoffManifestStore(
    var items: MutableMap<String, LenientCutoffManifest>,
    var order: MutableList<String>,
) : LenientCutoffManifestStore {
    override fun find(code: String): LenientCutoffManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCutoffManifest) {
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

    override fun list(): MutableList<LenientCutoffManifest> {
        val out = mutableListOf<LenientCutoffManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCutoffManifestStore(): LenientCutoffManifestStore {
    return MemoryLenientCutoffManifestStore(
        mutableMapOf<String, LenientCutoffManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCutoffManifestStore(
    var inner: LenientCutoffManifestStore,
    var cache: TtlCache<LenientCutoffManifest>,
    var hits: Int,
    var misses: Int,
) : LenientCutoffManifestStore {
    override fun find(code: String): LenientCutoffManifest? {
        val hit = (cache.get(code) as LenientCutoffManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCutoffManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCutoffManifest> = inner.list()
}

fun newCachedLenientCutoffManifestStore(inner: LenientCutoffManifestStore, ttl: Int, clock: Clock): LenientCutoffManifestStore {
    return CachedLenientCutoffManifestStore(
        inner = inner,
        cache = newTtlCache<LenientCutoffManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient cutoff manifests. Empty fields match all. */
data class LenientCutoffManifestQuery(
    var source: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCutoffManifest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCutoffManifest>): MutableList<LenientCutoffManifest> {
        val matched = filter(items) { candidate: LenientCutoffManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCutoffManifests(store: LenientCutoffManifestStore, query: LenientCutoffManifestQuery): MutableList<LenientCutoffManifest> {
    return query.apply(store.list())
}
