package com.acme.transport.customs.inspection.mapping

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary inspection profiles by code. */
interface PrimaryInspectionProfileStore {
    fun find(code: String): PrimaryInspectionProfile?
    fun save(item: PrimaryInspectionProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryInspectionProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryInspectionProfileStore(
    var items: MutableMap<String, PrimaryInspectionProfile>,
    var order: MutableList<String>,
) : PrimaryInspectionProfileStore {
    override fun find(code: String): PrimaryInspectionProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryInspectionProfile) {
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

    override fun list(): MutableList<PrimaryInspectionProfile> {
        val out = mutableListOf<PrimaryInspectionProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryInspectionProfileStore(): PrimaryInspectionProfileStore {
    return MemoryPrimaryInspectionProfileStore(
        mutableMapOf<String, PrimaryInspectionProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryInspectionProfileStore(
    var inner: PrimaryInspectionProfileStore,
    var cache: TtlCache<PrimaryInspectionProfile>,
    var hits: Int,
    var misses: Int,
) : PrimaryInspectionProfileStore {
    override fun find(code: String): PrimaryInspectionProfile? {
        val hit = (cache.get(code) as PrimaryInspectionProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryInspectionProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryInspectionProfile> = inner.list()
}

fun newCachedPrimaryInspectionProfileStore(inner: PrimaryInspectionProfileStore, ttl: Int, clock: Clock): PrimaryInspectionProfileStore {
    return CachedPrimaryInspectionProfileStore(
        inner = inner,
        cache = newTtlCache<PrimaryInspectionProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary inspection profiles. Empty fields match all. */
data class PrimaryInspectionProfileQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryInspectionProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryInspectionProfile>): MutableList<PrimaryInspectionProfile> {
        val matched = filter(items) { candidate: PrimaryInspectionProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryInspectionProfiles(store: PrimaryInspectionProfileStore, query: PrimaryInspectionProfileQuery): MutableList<PrimaryInspectionProfile> {
    return query.apply(store.list())
}
