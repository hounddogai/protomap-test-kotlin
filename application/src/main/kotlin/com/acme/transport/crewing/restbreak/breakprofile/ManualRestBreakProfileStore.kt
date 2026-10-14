package com.acme.transport.crewing.restbreak.breakprofile

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual rest break profiles by code. */
interface ManualRestBreakProfileStore {
    fun find(code: String): ManualRestBreakProfile?
    fun save(item: ManualRestBreakProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRestBreakProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRestBreakProfileStore(
    var items: MutableMap<String, ManualRestBreakProfile>,
    var order: MutableList<String>,
) : ManualRestBreakProfileStore {
    override fun find(code: String): ManualRestBreakProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRestBreakProfile) {
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

    override fun list(): MutableList<ManualRestBreakProfile> {
        val out = mutableListOf<ManualRestBreakProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRestBreakProfileStore(): ManualRestBreakProfileStore {
    return MemoryManualRestBreakProfileStore(
        mutableMapOf<String, ManualRestBreakProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRestBreakProfileStore(
    var inner: ManualRestBreakProfileStore,
    var cache: TtlCache<ManualRestBreakProfile>,
    var hits: Int,
    var misses: Int,
) : ManualRestBreakProfileStore {
    override fun find(code: String): ManualRestBreakProfile? {
        val hit = (cache.get(code) as ManualRestBreakProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRestBreakProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRestBreakProfile> = inner.list()
}

fun newCachedManualRestBreakProfileStore(inner: ManualRestBreakProfileStore, ttl: Int, clock: Clock): ManualRestBreakProfileStore {
    return CachedManualRestBreakProfileStore(
        inner = inner,
        cache = newTtlCache<ManualRestBreakProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual rest break profiles. Empty fields match all. */
data class ManualRestBreakProfileQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRestBreakProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRestBreakProfile>): MutableList<ManualRestBreakProfile> {
        val matched = filter(items) { candidate: ManualRestBreakProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRestBreakProfiles(store: ManualRestBreakProfileStore, query: ManualRestBreakProfileQuery): MutableList<ManualRestBreakProfile> {
    return query.apply(store.list())
}
