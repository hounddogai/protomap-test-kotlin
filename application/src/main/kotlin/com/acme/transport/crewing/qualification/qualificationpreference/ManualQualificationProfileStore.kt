package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual qualification profiles by code. */
interface ManualQualificationProfileStore {
    fun find(code: String): ManualQualificationProfile?
    fun save(item: ManualQualificationProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualQualificationProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualQualificationProfileStore(
    var items: MutableMap<String, ManualQualificationProfile>,
    var order: MutableList<String>,
) : ManualQualificationProfileStore {
    override fun find(code: String): ManualQualificationProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualQualificationProfile) {
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

    override fun list(): MutableList<ManualQualificationProfile> {
        val out = mutableListOf<ManualQualificationProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualQualificationProfileStore(): ManualQualificationProfileStore {
    return MemoryManualQualificationProfileStore(
        mutableMapOf<String, ManualQualificationProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualQualificationProfileStore(
    var inner: ManualQualificationProfileStore,
    var cache: TtlCache<ManualQualificationProfile>,
    var hits: Int,
    var misses: Int,
) : ManualQualificationProfileStore {
    override fun find(code: String): ManualQualificationProfile? {
        val hit = (cache.get(code) as ManualQualificationProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualQualificationProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualQualificationProfile> = inner.list()
}

fun newCachedManualQualificationProfileStore(inner: ManualQualificationProfileStore, ttl: Int, clock: Clock): ManualQualificationProfileStore {
    return CachedManualQualificationProfileStore(
        inner = inner,
        cache = newTtlCache<ManualQualificationProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual qualification profiles. Empty fields match all. */
data class ManualQualificationProfileQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualQualificationProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualQualificationProfile>): MutableList<ManualQualificationProfile> {
        val matched = filter(items) { candidate: ManualQualificationProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualQualificationProfiles(store: ManualQualificationProfileStore, query: ManualQualificationProfileQuery): MutableList<ManualQualificationProfile> {
    return query.apply(store.list())
}
