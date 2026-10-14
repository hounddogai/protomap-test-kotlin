package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling availability groups by code. */
interface RollingAvailabilityGroupStore {
    fun find(code: String): RollingAvailabilityGroup?
    fun save(item: RollingAvailabilityGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAvailabilityGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAvailabilityGroupStore(
    var items: MutableMap<String, RollingAvailabilityGroup>,
    var order: MutableList<String>,
) : RollingAvailabilityGroupStore {
    override fun find(code: String): RollingAvailabilityGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAvailabilityGroup) {
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

    override fun list(): MutableList<RollingAvailabilityGroup> {
        val out = mutableListOf<RollingAvailabilityGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAvailabilityGroupStore(): RollingAvailabilityGroupStore {
    return MemoryRollingAvailabilityGroupStore(
        mutableMapOf<String, RollingAvailabilityGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAvailabilityGroupStore(
    var inner: RollingAvailabilityGroupStore,
    var cache: TtlCache<RollingAvailabilityGroup>,
    var hits: Int,
    var misses: Int,
) : RollingAvailabilityGroupStore {
    override fun find(code: String): RollingAvailabilityGroup? {
        val hit = (cache.get(code) as RollingAvailabilityGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAvailabilityGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAvailabilityGroup> = inner.list()
}

fun newCachedRollingAvailabilityGroupStore(inner: RollingAvailabilityGroupStore, ttl: Int, clock: Clock): RollingAvailabilityGroupStore {
    return CachedRollingAvailabilityGroupStore(
        inner = inner,
        cache = newTtlCache<RollingAvailabilityGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling availability groups. Empty fields match all. */
data class RollingAvailabilityGroupQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAvailabilityGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RollingAvailabilityGroup>): MutableList<RollingAvailabilityGroup> {
        val matched = filter(items) { candidate: RollingAvailabilityGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAvailabilityGroups(store: RollingAvailabilityGroupStore, query: RollingAvailabilityGroupQuery): MutableList<RollingAvailabilityGroup> {
    return query.apply(store.list())
}
