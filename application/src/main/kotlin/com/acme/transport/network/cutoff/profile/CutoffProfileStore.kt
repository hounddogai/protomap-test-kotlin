package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff profiles by code. */
interface CutoffProfileStore {
    fun find(code: String): CutoffProfile?
    fun save(item: CutoffProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffProfileStore(
    var items: MutableMap<String, CutoffProfile>,
    var order: MutableList<String>,
) : CutoffProfileStore {
    override fun find(code: String): CutoffProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffProfile) {
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

    override fun list(): MutableList<CutoffProfile> {
        val out = mutableListOf<CutoffProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffProfileStore(): CutoffProfileStore {
    return MemoryCutoffProfileStore(mutableMapOf<String, CutoffProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffProfileStore(
    var inner: CutoffProfileStore,
    var cache: TtlCache<CutoffProfile>,
    var hits: Int,
    var misses: Int,
) : CutoffProfileStore {
    override fun find(code: String): CutoffProfile? {
        val hit = (cache.get(code) as CutoffProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffProfile> = inner.list()
}

fun newCachedCutoffProfileStore(inner: CutoffProfileStore, ttl: Int, clock: Clock): CutoffProfileStore {
    return CachedCutoffProfileStore(
        inner = inner,
        cache = newTtlCache<CutoffProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff profiles. Empty fields match all. */
data class CutoffProfileQuery(
    var source: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffProfile): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffProfile>): MutableList<CutoffProfile> {
        val matched = filter(items) { candidate: CutoffProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffProfiles(store: CutoffProfileStore, query: CutoffProfileQuery): MutableList<CutoffProfile> {
    return query.apply(store.list())
}
