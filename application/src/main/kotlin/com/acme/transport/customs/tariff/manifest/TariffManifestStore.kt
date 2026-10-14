package com.acme.transport.customs.tariff.manifest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff manifests by code. */
interface TariffManifestStore {
    fun find(code: String): TariffManifest?
    fun save(item: TariffManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffManifestStore(
    var items: MutableMap<String, TariffManifest>,
    var order: MutableList<String>,
) : TariffManifestStore {
    override fun find(code: String): TariffManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffManifest) {
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

    override fun list(): MutableList<TariffManifest> {
        val out = mutableListOf<TariffManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffManifestStore(): TariffManifestStore {
    return MemoryTariffManifestStore(mutableMapOf<String, TariffManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffManifestStore(
    var inner: TariffManifestStore,
    var cache: TtlCache<TariffManifest>,
    var hits: Int,
    var misses: Int,
) : TariffManifestStore {
    override fun find(code: String): TariffManifest? {
        val hit = (cache.get(code) as TariffManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffManifest> = inner.list()
}

fun newCachedTariffManifestStore(inner: TariffManifestStore, ttl: Int, clock: Clock): TariffManifestStore {
    return CachedTariffManifestStore(
        inner = inner,
        cache = newTtlCache<TariffManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff manifests. Empty fields match all. */
data class TariffManifestQuery(
    var category: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffManifest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffManifest>): MutableList<TariffManifest> {
        val matched = filter(items) { candidate: TariffManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffManifests(store: TariffManifestStore, query: TariffManifestQuery): MutableList<TariffManifest> {
    return query.apply(store.list())
}
