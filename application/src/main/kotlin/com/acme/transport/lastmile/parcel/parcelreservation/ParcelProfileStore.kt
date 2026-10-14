package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel profiles by code. */
interface ParcelProfileStore {
    fun find(code: String): ParcelProfile?
    fun save(item: ParcelProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelProfileStore(
    var items: MutableMap<String, ParcelProfile>,
    var order: MutableList<String>,
) : ParcelProfileStore {
    override fun find(code: String): ParcelProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelProfile) {
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

    override fun list(): MutableList<ParcelProfile> {
        val out = mutableListOf<ParcelProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelProfileStore(): ParcelProfileStore {
    return MemoryParcelProfileStore(mutableMapOf<String, ParcelProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelProfileStore(
    var inner: ParcelProfileStore,
    var cache: TtlCache<ParcelProfile>,
    var hits: Int,
    var misses: Int,
) : ParcelProfileStore {
    override fun find(code: String): ParcelProfile? {
        val hit = (cache.get(code) as ParcelProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelProfile> = inner.list()
}

fun newCachedParcelProfileStore(inner: ParcelProfileStore, ttl: Int, clock: Clock): ParcelProfileStore {
    return CachedParcelProfileStore(
        inner = inner,
        cache = newTtlCache<ParcelProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel profiles. Empty fields match all. */
data class ParcelProfileQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelProfile>): MutableList<ParcelProfile> {
        val matched = filter(items) { candidate: ParcelProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelProfiles(store: ParcelProfileStore, query: ParcelProfileQuery): MutableList<ParcelProfile> {
    return query.apply(store.list())
}
