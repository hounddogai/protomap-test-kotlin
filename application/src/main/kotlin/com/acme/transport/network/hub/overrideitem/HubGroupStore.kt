package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub groups by code. */
interface HubGroupStore {
    fun find(code: String): HubGroup?
    fun save(item: HubGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubGroupStore(
    var items: MutableMap<String, HubGroup>,
    var order: MutableList<String>,
) : HubGroupStore {
    override fun find(code: String): HubGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubGroup) {
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

    override fun list(): MutableList<HubGroup> {
        val out = mutableListOf<HubGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubGroupStore(): HubGroupStore {
    return MemoryHubGroupStore(mutableMapOf<String, HubGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubGroupStore(
    var inner: HubGroupStore,
    var cache: TtlCache<HubGroup>,
    var hits: Int,
    var misses: Int,
) : HubGroupStore {
    override fun find(code: String): HubGroup? {
        val hit = (cache.get(code) as HubGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubGroup> = inner.list()
}

fun newCachedHubGroupStore(inner: HubGroupStore, ttl: Int, clock: Clock): HubGroupStore {
    return CachedHubGroupStore(
        inner = inner,
        cache = newTtlCache<HubGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub groups. Empty fields match all. */
data class HubGroupQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubGroup): Boolean {
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

    fun apply(items: MutableList<HubGroup>): MutableList<HubGroup> {
        val matched = filter(items) { candidate: HubGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubGroups(store: HubGroupStore, query: HubGroupQuery): MutableList<HubGroup> = query.apply(store.list())
