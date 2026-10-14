package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop groups by code. */
interface DropGroupStore {
    fun find(code: String): DropGroup?
    fun save(item: DropGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropGroupStore(
    var items: MutableMap<String, DropGroup>,
    var order: MutableList<String>,
) : DropGroupStore {
    override fun find(code: String): DropGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropGroup) {
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

    override fun list(): MutableList<DropGroup> {
        val out = mutableListOf<DropGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropGroupStore(): DropGroupStore {
    return MemoryDropGroupStore(mutableMapOf<String, DropGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropGroupStore(
    var inner: DropGroupStore,
    var cache: TtlCache<DropGroup>,
    var hits: Int,
    var misses: Int,
) : DropGroupStore {
    override fun find(code: String): DropGroup? {
        val hit = (cache.get(code) as DropGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropGroup> = inner.list()
}

fun newCachedDropGroupStore(inner: DropGroupStore, ttl: Int, clock: Clock): DropGroupStore {
    return CachedDropGroupStore(
        inner = inner,
        cache = newTtlCache<DropGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop groups. Empty fields match all. */
data class DropGroupQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropGroup>): MutableList<DropGroup> {
        val matched = filter(items) { candidate: DropGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropGroups(store: DropGroupStore, query: DropGroupQuery): MutableList<DropGroup> {
    return query.apply(store.list())
}
