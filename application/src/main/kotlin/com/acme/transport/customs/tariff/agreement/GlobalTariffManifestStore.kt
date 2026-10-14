package com.acme.transport.customs.tariff.agreement

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global tariff manifests by code. */
interface GlobalTariffManifestStore {
    fun find(code: String): GlobalTariffManifest?
    fun save(item: GlobalTariffManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTariffManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTariffManifestStore(
    var items: MutableMap<String, GlobalTariffManifest>,
    var order: MutableList<String>,
) : GlobalTariffManifestStore {
    override fun find(code: String): GlobalTariffManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTariffManifest) {
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

    override fun list(): MutableList<GlobalTariffManifest> {
        val out = mutableListOf<GlobalTariffManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTariffManifestStore(): GlobalTariffManifestStore {
    return MemoryGlobalTariffManifestStore(
        mutableMapOf<String, GlobalTariffManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTariffManifestStore(
    var inner: GlobalTariffManifestStore,
    var cache: TtlCache<GlobalTariffManifest>,
    var hits: Int,
    var misses: Int,
) : GlobalTariffManifestStore {
    override fun find(code: String): GlobalTariffManifest? {
        val hit = (cache.get(code) as GlobalTariffManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTariffManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTariffManifest> = inner.list()
}

fun newCachedGlobalTariffManifestStore(inner: GlobalTariffManifestStore, ttl: Int, clock: Clock): GlobalTariffManifestStore {
    return CachedGlobalTariffManifestStore(
        inner = inner,
        cache = newTtlCache<GlobalTariffManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global tariff manifests. Empty fields match all. */
data class GlobalTariffManifestQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTariffManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTariffManifest>): MutableList<GlobalTariffManifest> {
        val matched = filter(items) { candidate: GlobalTariffManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTariffManifests(store: GlobalTariffManifestStore, query: GlobalTariffManifestQuery): MutableList<GlobalTariffManifest> {
    return query.apply(store.list())
}
