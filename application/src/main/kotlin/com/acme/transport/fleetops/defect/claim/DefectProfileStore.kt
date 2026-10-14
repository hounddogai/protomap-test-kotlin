package com.acme.transport.fleetops.defect.claim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect profiles by code. */
interface DefectProfileStore {
    fun find(code: String): DefectProfile?
    fun save(item: DefectProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectProfileStore(
    var items: MutableMap<String, DefectProfile>,
    var order: MutableList<String>,
) : DefectProfileStore {
    override fun find(code: String): DefectProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectProfile) {
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

    override fun list(): MutableList<DefectProfile> {
        val out = mutableListOf<DefectProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectProfileStore(): DefectProfileStore {
    return MemoryDefectProfileStore(mutableMapOf<String, DefectProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectProfileStore(
    var inner: DefectProfileStore,
    var cache: TtlCache<DefectProfile>,
    var hits: Int,
    var misses: Int,
) : DefectProfileStore {
    override fun find(code: String): DefectProfile? {
        val hit = (cache.get(code) as DefectProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectProfile> = inner.list()
}

fun newCachedDefectProfileStore(inner: DefectProfileStore, ttl: Int, clock: Clock): DefectProfileStore {
    return CachedDefectProfileStore(
        inner = inner,
        cache = newTtlCache<DefectProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect profiles. Empty fields match all. */
data class DefectProfileQuery(
    var channel: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectProfile): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectProfile>): MutableList<DefectProfile> {
        val matched = filter(items) { candidate: DefectProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectProfiles(store: DefectProfileStore, query: DefectProfileQuery): MutableList<DefectProfile> {
    return query.apply(store.list())
}
