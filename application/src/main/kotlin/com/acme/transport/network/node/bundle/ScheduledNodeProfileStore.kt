package com.acme.transport.network.node.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled node profiles by code. */
interface ScheduledNodeProfileStore {
    fun find(code: String): ScheduledNodeProfile?
    fun save(item: ScheduledNodeProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledNodeProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledNodeProfileStore(
    var items: MutableMap<String, ScheduledNodeProfile>,
    var order: MutableList<String>,
) : ScheduledNodeProfileStore {
    override fun find(code: String): ScheduledNodeProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledNodeProfile) {
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

    override fun list(): MutableList<ScheduledNodeProfile> {
        val out = mutableListOf<ScheduledNodeProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledNodeProfileStore(): ScheduledNodeProfileStore {
    return MemoryScheduledNodeProfileStore(
        mutableMapOf<String, ScheduledNodeProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledNodeProfileStore(
    var inner: ScheduledNodeProfileStore,
    var cache: TtlCache<ScheduledNodeProfile>,
    var hits: Int,
    var misses: Int,
) : ScheduledNodeProfileStore {
    override fun find(code: String): ScheduledNodeProfile? {
        val hit = (cache.get(code) as ScheduledNodeProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledNodeProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledNodeProfile> = inner.list()
}

fun newCachedScheduledNodeProfileStore(inner: ScheduledNodeProfileStore, ttl: Int, clock: Clock): ScheduledNodeProfileStore {
    return CachedScheduledNodeProfileStore(
        inner = inner,
        cache = newTtlCache<ScheduledNodeProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled node profiles. Empty fields match all. */
data class ScheduledNodeProfileQuery(
    var locale: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledNodeProfile): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledNodeProfile>): MutableList<ScheduledNodeProfile> {
        val matched = filter(items) { candidate: ScheduledNodeProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledNodeProfiles(store: ScheduledNodeProfileStore, query: ScheduledNodeProfileQuery): MutableList<ScheduledNodeProfile> {
    return query.apply(store.list())
}
