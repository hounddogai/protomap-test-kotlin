package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender profiles by code. */
interface TenderProfileStore {
    fun find(code: String): TenderProfile?
    fun save(item: TenderProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderProfileStore(
    var items: MutableMap<String, TenderProfile>,
    var order: MutableList<String>,
) : TenderProfileStore {
    override fun find(code: String): TenderProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderProfile) {
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

    override fun list(): MutableList<TenderProfile> {
        val out = mutableListOf<TenderProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderProfileStore(): TenderProfileStore {
    return MemoryTenderProfileStore(mutableMapOf<String, TenderProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderProfileStore(
    var inner: TenderProfileStore,
    var cache: TtlCache<TenderProfile>,
    var hits: Int,
    var misses: Int,
) : TenderProfileStore {
    override fun find(code: String): TenderProfile? {
        val hit = (cache.get(code) as TenderProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderProfile> = inner.list()
}

fun newCachedTenderProfileStore(inner: TenderProfileStore, ttl: Int, clock: Clock): TenderProfileStore {
    return CachedTenderProfileStore(
        inner = inner,
        cache = newTtlCache<TenderProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender profiles. Empty fields match all. */
data class TenderProfileQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TenderProfile>): MutableList<TenderProfile> {
        val matched = filter(items) { candidate: TenderProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderProfiles(store: TenderProfileStore, query: TenderProfileQuery): MutableList<TenderProfile> {
    return query.apply(store.list())
}
