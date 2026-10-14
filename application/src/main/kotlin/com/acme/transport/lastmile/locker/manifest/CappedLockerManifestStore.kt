package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped locker manifests by code. */
interface CappedLockerManifestStore {
    fun find(code: String): CappedLockerManifest?
    fun save(item: CappedLockerManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedLockerManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedLockerManifestStore(
    var items: MutableMap<String, CappedLockerManifest>,
    var order: MutableList<String>,
) : CappedLockerManifestStore {
    override fun find(code: String): CappedLockerManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedLockerManifest) {
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

    override fun list(): MutableList<CappedLockerManifest> {
        val out = mutableListOf<CappedLockerManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedLockerManifestStore(): CappedLockerManifestStore {
    return MemoryCappedLockerManifestStore(
        mutableMapOf<String, CappedLockerManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedLockerManifestStore(
    var inner: CappedLockerManifestStore,
    var cache: TtlCache<CappedLockerManifest>,
    var hits: Int,
    var misses: Int,
) : CappedLockerManifestStore {
    override fun find(code: String): CappedLockerManifest? {
        val hit = (cache.get(code) as CappedLockerManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedLockerManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedLockerManifest> = inner.list()
}

fun newCachedCappedLockerManifestStore(inner: CappedLockerManifestStore, ttl: Int, clock: Clock): CappedLockerManifestStore {
    return CachedCappedLockerManifestStore(
        inner = inner,
        cache = newTtlCache<CappedLockerManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped locker manifests. Empty fields match all. */
data class CappedLockerManifestQuery(
    var locale: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedLockerManifest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedLockerManifest>): MutableList<CappedLockerManifest> {
        val matched = filter(items) { candidate: CappedLockerManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedLockerManifests(store: CappedLockerManifestStore, query: CappedLockerManifestQuery): MutableList<CappedLockerManifest> {
    return query.apply(store.list())
}
