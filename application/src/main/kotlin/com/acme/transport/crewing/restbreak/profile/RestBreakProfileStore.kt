package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break profiles by code. */
interface RestBreakProfileStore {
    fun find(code: String): RestBreakProfile?
    fun save(item: RestBreakProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakProfileStore(
    var items: MutableMap<String, RestBreakProfile>,
    var order: MutableList<String>,
) : RestBreakProfileStore {
    override fun find(code: String): RestBreakProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakProfile) {
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

    override fun list(): MutableList<RestBreakProfile> {
        val out = mutableListOf<RestBreakProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakProfileStore(): RestBreakProfileStore {
    return MemoryRestBreakProfileStore(mutableMapOf<String, RestBreakProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakProfileStore(
    var inner: RestBreakProfileStore,
    var cache: TtlCache<RestBreakProfile>,
    var hits: Int,
    var misses: Int,
) : RestBreakProfileStore {
    override fun find(code: String): RestBreakProfile? {
        val hit = (cache.get(code) as RestBreakProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakProfile> = inner.list()
}

fun newCachedRestBreakProfileStore(inner: RestBreakProfileStore, ttl: Int, clock: Clock): RestBreakProfileStore {
    return CachedRestBreakProfileStore(
        inner = inner,
        cache = newTtlCache<RestBreakProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break profiles. Empty fields match all. */
data class RestBreakProfileQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakProfile>): MutableList<RestBreakProfile> {
        val matched = filter(items) { candidate: RestBreakProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakProfiles(store: RestBreakProfileStore, query: RestBreakProfileQuery): MutableList<RestBreakProfile> {
    return query.apply(store.list())
}
