package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy near miss groups by code. */
interface LegacyNearMissGroupStore {
    fun find(code: String): LegacyNearMissGroup?
    fun save(item: LegacyNearMissGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyNearMissGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyNearMissGroupStore(
    var items: MutableMap<String, LegacyNearMissGroup>,
    var order: MutableList<String>,
) : LegacyNearMissGroupStore {
    override fun find(code: String): LegacyNearMissGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyNearMissGroup) {
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

    override fun list(): MutableList<LegacyNearMissGroup> {
        val out = mutableListOf<LegacyNearMissGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyNearMissGroupStore(): LegacyNearMissGroupStore {
    return MemoryLegacyNearMissGroupStore(mutableMapOf<String, LegacyNearMissGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyNearMissGroupStore(
    var inner: LegacyNearMissGroupStore,
    var cache: TtlCache<LegacyNearMissGroup>,
    var hits: Int,
    var misses: Int,
) : LegacyNearMissGroupStore {
    override fun find(code: String): LegacyNearMissGroup? {
        val hit = (cache.get(code) as LegacyNearMissGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyNearMissGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyNearMissGroup> = inner.list()
}

fun newCachedLegacyNearMissGroupStore(inner: LegacyNearMissGroupStore, ttl: Int, clock: Clock): LegacyNearMissGroupStore {
    return CachedLegacyNearMissGroupStore(
        inner = inner,
        cache = newTtlCache<LegacyNearMissGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy near miss groups. Empty fields match all. */
data class LegacyNearMissGroupQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyNearMissGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyNearMissGroup>): MutableList<LegacyNearMissGroup> {
        val matched = filter(items) { candidate: LegacyNearMissGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyNearMissGroups(store: LegacyNearMissGroupStore, query: LegacyNearMissGroupQuery): MutableList<LegacyNearMissGroup> {
    return query.apply(store.list())
}
