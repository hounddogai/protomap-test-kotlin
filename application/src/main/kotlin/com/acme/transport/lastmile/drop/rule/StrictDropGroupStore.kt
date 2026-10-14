package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict drop groups by code. */
interface StrictDropGroupStore {
    fun find(code: String): StrictDropGroup?
    fun save(item: StrictDropGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDropGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDropGroupStore(
    var items: MutableMap<String, StrictDropGroup>,
    var order: MutableList<String>,
) : StrictDropGroupStore {
    override fun find(code: String): StrictDropGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDropGroup) {
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

    override fun list(): MutableList<StrictDropGroup> {
        val out = mutableListOf<StrictDropGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDropGroupStore(): StrictDropGroupStore {
    return MemoryStrictDropGroupStore(mutableMapOf<String, StrictDropGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDropGroupStore(
    var inner: StrictDropGroupStore,
    var cache: TtlCache<StrictDropGroup>,
    var hits: Int,
    var misses: Int,
) : StrictDropGroupStore {
    override fun find(code: String): StrictDropGroup? {
        val hit = (cache.get(code) as StrictDropGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDropGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDropGroup> = inner.list()
}

fun newCachedStrictDropGroupStore(inner: StrictDropGroupStore, ttl: Int, clock: Clock): StrictDropGroupStore {
    return CachedStrictDropGroupStore(
        inner = inner,
        cache = newTtlCache<StrictDropGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict drop groups. Empty fields match all. */
data class StrictDropGroupQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDropGroup): Boolean {
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

    fun apply(items: MutableList<StrictDropGroup>): MutableList<StrictDropGroup> {
        val matched = filter(items) { candidate: StrictDropGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDropGroups(store: StrictDropGroupStore, query: StrictDropGroupQuery): MutableList<StrictDropGroup> {
    return query.apply(store.list())
}
