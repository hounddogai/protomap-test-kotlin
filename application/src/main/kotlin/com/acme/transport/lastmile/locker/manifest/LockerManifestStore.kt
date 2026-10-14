package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker manifests by code. */
interface LockerManifestStore {
    fun find(code: String): LockerManifest?
    fun save(item: LockerManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerManifestStore(
    var items: MutableMap<String, LockerManifest>,
    var order: MutableList<String>,
) : LockerManifestStore {
    override fun find(code: String): LockerManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerManifest) {
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

    override fun list(): MutableList<LockerManifest> {
        val out = mutableListOf<LockerManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerManifestStore(): LockerManifestStore {
    return MemoryLockerManifestStore(mutableMapOf<String, LockerManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerManifestStore(
    var inner: LockerManifestStore,
    var cache: TtlCache<LockerManifest>,
    var hits: Int,
    var misses: Int,
) : LockerManifestStore {
    override fun find(code: String): LockerManifest? {
        val hit = (cache.get(code) as LockerManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerManifest> = inner.list()
}

fun newCachedLockerManifestStore(inner: LockerManifestStore, ttl: Int, clock: Clock): LockerManifestStore {
    return CachedLockerManifestStore(
        inner = inner,
        cache = newTtlCache<LockerManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker manifests. Empty fields match all. */
data class LockerManifestQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LockerManifest>): MutableList<LockerManifest> {
        val matched = filter(items) { candidate: LockerManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerManifests(store: LockerManifestStore, query: LockerManifestQuery): MutableList<LockerManifest> {
    return query.apply(store.list())
}
