package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global rest break profiles by code. */
interface GlobalRestBreakProfileStore {
    fun find(code: String): GlobalRestBreakProfile?
    fun save(item: GlobalRestBreakProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRestBreakProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRestBreakProfileStore(
    var items: MutableMap<String, GlobalRestBreakProfile>,
    var order: MutableList<String>,
) : GlobalRestBreakProfileStore {
    override fun find(code: String): GlobalRestBreakProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRestBreakProfile) {
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

    override fun list(): MutableList<GlobalRestBreakProfile> {
        val out = mutableListOf<GlobalRestBreakProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRestBreakProfileStore(): GlobalRestBreakProfileStore {
    return MemoryGlobalRestBreakProfileStore(
        mutableMapOf<String, GlobalRestBreakProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRestBreakProfileStore(
    var inner: GlobalRestBreakProfileStore,
    var cache: TtlCache<GlobalRestBreakProfile>,
    var hits: Int,
    var misses: Int,
) : GlobalRestBreakProfileStore {
    override fun find(code: String): GlobalRestBreakProfile? {
        val hit = (cache.get(code) as GlobalRestBreakProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRestBreakProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRestBreakProfile> = inner.list()
}

fun newCachedGlobalRestBreakProfileStore(inner: GlobalRestBreakProfileStore, ttl: Int, clock: Clock): GlobalRestBreakProfileStore {
    return CachedGlobalRestBreakProfileStore(
        inner = inner,
        cache = newTtlCache<GlobalRestBreakProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global rest break profiles. Empty fields match all. */
data class GlobalRestBreakProfileQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRestBreakProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRestBreakProfile>): MutableList<GlobalRestBreakProfile> {
        val matched = filter(items) { candidate: GlobalRestBreakProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRestBreakProfiles(store: GlobalRestBreakProfileStore, query: GlobalRestBreakProfileQuery): MutableList<GlobalRestBreakProfile> {
    return query.apply(store.list())
}
