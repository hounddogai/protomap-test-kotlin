package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global parcel profiles by code. */
interface GlobalParcelProfileStore {
    fun find(code: String): GlobalParcelProfile?
    fun save(item: GlobalParcelProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalParcelProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalParcelProfileStore(
    var items: MutableMap<String, GlobalParcelProfile>,
    var order: MutableList<String>,
) : GlobalParcelProfileStore {
    override fun find(code: String): GlobalParcelProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalParcelProfile) {
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

    override fun list(): MutableList<GlobalParcelProfile> {
        val out = mutableListOf<GlobalParcelProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalParcelProfileStore(): GlobalParcelProfileStore {
    return MemoryGlobalParcelProfileStore(mutableMapOf<String, GlobalParcelProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalParcelProfileStore(
    var inner: GlobalParcelProfileStore,
    var cache: TtlCache<GlobalParcelProfile>,
    var hits: Int,
    var misses: Int,
) : GlobalParcelProfileStore {
    override fun find(code: String): GlobalParcelProfile? {
        val hit = (cache.get(code) as GlobalParcelProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalParcelProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalParcelProfile> = inner.list()
}

fun newCachedGlobalParcelProfileStore(inner: GlobalParcelProfileStore, ttl: Int, clock: Clock): GlobalParcelProfileStore {
    return CachedGlobalParcelProfileStore(
        inner = inner,
        cache = newTtlCache<GlobalParcelProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global parcel profiles. Empty fields match all. */
data class GlobalParcelProfileQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalParcelProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalParcelProfile>): MutableList<GlobalParcelProfile> {
        val matched = filter(items) { candidate: GlobalParcelProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalParcelProfiles(store: GlobalParcelProfileStore, query: GlobalParcelProfileQuery): MutableList<GlobalParcelProfile> {
    return query.apply(store.list())
}
