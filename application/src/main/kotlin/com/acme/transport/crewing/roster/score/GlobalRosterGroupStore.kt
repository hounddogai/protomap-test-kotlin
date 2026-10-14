package com.acme.transport.crewing.roster.score

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global roster groups by code. */
interface GlobalRosterGroupStore {
    fun find(code: String): GlobalRosterGroup?
    fun save(item: GlobalRosterGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRosterGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRosterGroupStore(
    var items: MutableMap<String, GlobalRosterGroup>,
    var order: MutableList<String>,
) : GlobalRosterGroupStore {
    override fun find(code: String): GlobalRosterGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRosterGroup) {
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

    override fun list(): MutableList<GlobalRosterGroup> {
        val out = mutableListOf<GlobalRosterGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRosterGroupStore(): GlobalRosterGroupStore {
    return MemoryGlobalRosterGroupStore(mutableMapOf<String, GlobalRosterGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRosterGroupStore(
    var inner: GlobalRosterGroupStore,
    var cache: TtlCache<GlobalRosterGroup>,
    var hits: Int,
    var misses: Int,
) : GlobalRosterGroupStore {
    override fun find(code: String): GlobalRosterGroup? {
        val hit = (cache.get(code) as GlobalRosterGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRosterGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRosterGroup> = inner.list()
}

fun newCachedGlobalRosterGroupStore(inner: GlobalRosterGroupStore, ttl: Int, clock: Clock): GlobalRosterGroupStore {
    return CachedGlobalRosterGroupStore(
        inner = inner,
        cache = newTtlCache<GlobalRosterGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global roster groups. Empty fields match all. */
data class GlobalRosterGroupQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRosterGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<GlobalRosterGroup>): MutableList<GlobalRosterGroup> {
        val matched = filter(items) { candidate: GlobalRosterGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRosterGroups(store: GlobalRosterGroupStore, query: GlobalRosterGroupQuery): MutableList<GlobalRosterGroup> {
    return query.apply(store.list())
}
