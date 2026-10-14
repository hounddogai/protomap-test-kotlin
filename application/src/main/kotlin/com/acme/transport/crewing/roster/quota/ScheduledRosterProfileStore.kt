package com.acme.transport.crewing.roster.quota

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled roster profiles by code. */
interface ScheduledRosterProfileStore {
    fun find(code: String): ScheduledRosterProfile?
    fun save(item: ScheduledRosterProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRosterProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRosterProfileStore(
    var items: MutableMap<String, ScheduledRosterProfile>,
    var order: MutableList<String>,
) : ScheduledRosterProfileStore {
    override fun find(code: String): ScheduledRosterProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRosterProfile) {
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

    override fun list(): MutableList<ScheduledRosterProfile> {
        val out = mutableListOf<ScheduledRosterProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRosterProfileStore(): ScheduledRosterProfileStore {
    return MemoryScheduledRosterProfileStore(
        mutableMapOf<String, ScheduledRosterProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRosterProfileStore(
    var inner: ScheduledRosterProfileStore,
    var cache: TtlCache<ScheduledRosterProfile>,
    var hits: Int,
    var misses: Int,
) : ScheduledRosterProfileStore {
    override fun find(code: String): ScheduledRosterProfile? {
        val hit = (cache.get(code) as ScheduledRosterProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRosterProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRosterProfile> = inner.list()
}

fun newCachedScheduledRosterProfileStore(inner: ScheduledRosterProfileStore, ttl: Int, clock: Clock): ScheduledRosterProfileStore {
    return CachedScheduledRosterProfileStore(
        inner = inner,
        cache = newTtlCache<ScheduledRosterProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled roster profiles. Empty fields match all. */
data class ScheduledRosterProfileQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRosterProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ScheduledRosterProfile>): MutableList<ScheduledRosterProfile> {
        val matched = filter(items) { candidate: ScheduledRosterProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRosterProfiles(store: ScheduledRosterProfileStore, query: ScheduledRosterProfileQuery): MutableList<ScheduledRosterProfile> {
    return query.apply(store.list())
}
