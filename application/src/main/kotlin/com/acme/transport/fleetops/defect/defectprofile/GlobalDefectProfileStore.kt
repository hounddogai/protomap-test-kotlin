package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global defect profiles by code. */
interface GlobalDefectProfileStore {
    fun find(code: String): GlobalDefectProfile?
    fun save(item: GlobalDefectProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDefectProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDefectProfileStore(
    var items: MutableMap<String, GlobalDefectProfile>,
    var order: MutableList<String>,
) : GlobalDefectProfileStore {
    override fun find(code: String): GlobalDefectProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDefectProfile) {
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

    override fun list(): MutableList<GlobalDefectProfile> {
        val out = mutableListOf<GlobalDefectProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDefectProfileStore(): GlobalDefectProfileStore {
    return MemoryGlobalDefectProfileStore(mutableMapOf<String, GlobalDefectProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDefectProfileStore(
    var inner: GlobalDefectProfileStore,
    var cache: TtlCache<GlobalDefectProfile>,
    var hits: Int,
    var misses: Int,
) : GlobalDefectProfileStore {
    override fun find(code: String): GlobalDefectProfile? {
        val hit = (cache.get(code) as GlobalDefectProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDefectProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDefectProfile> = inner.list()
}

fun newCachedGlobalDefectProfileStore(inner: GlobalDefectProfileStore, ttl: Int, clock: Clock): GlobalDefectProfileStore {
    return CachedGlobalDefectProfileStore(
        inner = inner,
        cache = newTtlCache<GlobalDefectProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global defect profiles. Empty fields match all. */
data class GlobalDefectProfileQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDefectProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDefectProfile>): MutableList<GlobalDefectProfile> {
        val matched = filter(items) { candidate: GlobalDefectProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDefectProfiles(store: GlobalDefectProfileStore, query: GlobalDefectProfileQuery): MutableList<GlobalDefectProfile> {
    return query.apply(store.list())
}
