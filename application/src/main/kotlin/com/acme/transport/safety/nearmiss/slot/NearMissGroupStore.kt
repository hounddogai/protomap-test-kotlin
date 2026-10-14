package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss groups by code. */
interface NearMissGroupStore {
    fun find(code: String): NearMissGroup?
    fun save(item: NearMissGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissGroupStore(
    var items: MutableMap<String, NearMissGroup>,
    var order: MutableList<String>,
) : NearMissGroupStore {
    override fun find(code: String): NearMissGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissGroup) {
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

    override fun list(): MutableList<NearMissGroup> {
        val out = mutableListOf<NearMissGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissGroupStore(): NearMissGroupStore {
    return MemoryNearMissGroupStore(mutableMapOf<String, NearMissGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissGroupStore(
    var inner: NearMissGroupStore,
    var cache: TtlCache<NearMissGroup>,
    var hits: Int,
    var misses: Int,
) : NearMissGroupStore {
    override fun find(code: String): NearMissGroup? {
        val hit = (cache.get(code) as NearMissGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissGroup> = inner.list()
}

fun newCachedNearMissGroupStore(inner: NearMissGroupStore, ttl: Int, clock: Clock): NearMissGroupStore {
    return CachedNearMissGroupStore(
        inner = inner,
        cache = newTtlCache<NearMissGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss groups. Empty fields match all. */
data class NearMissGroupQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissGroup>): MutableList<NearMissGroup> {
        val matched = filter(items) { candidate: NearMissGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissGroups(store: NearMissGroupStore, query: NearMissGroupQuery): MutableList<NearMissGroup> {
    return query.apply(store.list())
}
