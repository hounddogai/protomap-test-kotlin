package com.acme.transport.crewing.qualification.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered qualification profiles by code. */
interface TieredQualificationProfileStore {
    fun find(code: String): TieredQualificationProfile?
    fun save(item: TieredQualificationProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredQualificationProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredQualificationProfileStore(
    var items: MutableMap<String, TieredQualificationProfile>,
    var order: MutableList<String>,
) : TieredQualificationProfileStore {
    override fun find(code: String): TieredQualificationProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredQualificationProfile) {
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

    override fun list(): MutableList<TieredQualificationProfile> {
        val out = mutableListOf<TieredQualificationProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredQualificationProfileStore(): TieredQualificationProfileStore {
    return MemoryTieredQualificationProfileStore(
        mutableMapOf<String, TieredQualificationProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredQualificationProfileStore(
    var inner: TieredQualificationProfileStore,
    var cache: TtlCache<TieredQualificationProfile>,
    var hits: Int,
    var misses: Int,
) : TieredQualificationProfileStore {
    override fun find(code: String): TieredQualificationProfile? {
        val hit = (cache.get(code) as TieredQualificationProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredQualificationProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredQualificationProfile> = inner.list()
}

fun newCachedTieredQualificationProfileStore(inner: TieredQualificationProfileStore, ttl: Int, clock: Clock): TieredQualificationProfileStore {
    return CachedTieredQualificationProfileStore(
        inner = inner,
        cache = newTtlCache<TieredQualificationProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered qualification profiles. Empty fields match all. */
data class TieredQualificationProfileQuery(
    var source: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredQualificationProfile): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<TieredQualificationProfile>): MutableList<TieredQualificationProfile> {
        val matched = filter(items) { candidate: TieredQualificationProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredQualificationProfiles(store: TieredQualificationProfileStore, query: TieredQualificationProfileQuery): MutableList<TieredQualificationProfile> {
    return query.apply(store.list())
}
