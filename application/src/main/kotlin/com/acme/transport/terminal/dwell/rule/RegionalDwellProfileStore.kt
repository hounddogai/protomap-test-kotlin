package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional dwell profiles by code. */
interface RegionalDwellProfileStore {
    fun find(code: String): RegionalDwellProfile?
    fun save(item: RegionalDwellProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDwellProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDwellProfileStore(
    var items: MutableMap<String, RegionalDwellProfile>,
    var order: MutableList<String>,
) : RegionalDwellProfileStore {
    override fun find(code: String): RegionalDwellProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDwellProfile) {
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

    override fun list(): MutableList<RegionalDwellProfile> {
        val out = mutableListOf<RegionalDwellProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDwellProfileStore(): RegionalDwellProfileStore {
    return MemoryRegionalDwellProfileStore(
        mutableMapOf<String, RegionalDwellProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDwellProfileStore(
    var inner: RegionalDwellProfileStore,
    var cache: TtlCache<RegionalDwellProfile>,
    var hits: Int,
    var misses: Int,
) : RegionalDwellProfileStore {
    override fun find(code: String): RegionalDwellProfile? {
        val hit = (cache.get(code) as RegionalDwellProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDwellProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDwellProfile> = inner.list()
}

fun newCachedRegionalDwellProfileStore(inner: RegionalDwellProfileStore, ttl: Int, clock: Clock): RegionalDwellProfileStore {
    return CachedRegionalDwellProfileStore(
        inner = inner,
        cache = newTtlCache<RegionalDwellProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional dwell profiles. Empty fields match all. */
data class RegionalDwellProfileQuery(
    var segment: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDwellProfile): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDwellProfile>): MutableList<RegionalDwellProfile> {
        val matched = filter(items) { candidate: RegionalDwellProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDwellProfiles(store: RegionalDwellProfileStore, query: RegionalDwellProfileQuery): MutableList<RegionalDwellProfile> {
    return query.apply(store.list())
}
