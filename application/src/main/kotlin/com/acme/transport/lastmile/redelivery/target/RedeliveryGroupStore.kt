package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery groups by code. */
interface RedeliveryGroupStore {
    fun find(code: String): RedeliveryGroup?
    fun save(item: RedeliveryGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryGroupStore(
    var items: MutableMap<String, RedeliveryGroup>,
    var order: MutableList<String>,
) : RedeliveryGroupStore {
    override fun find(code: String): RedeliveryGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryGroup) {
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

    override fun list(): MutableList<RedeliveryGroup> {
        val out = mutableListOf<RedeliveryGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryGroupStore(): RedeliveryGroupStore {
    return MemoryRedeliveryGroupStore(mutableMapOf<String, RedeliveryGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryGroupStore(
    var inner: RedeliveryGroupStore,
    var cache: TtlCache<RedeliveryGroup>,
    var hits: Int,
    var misses: Int,
) : RedeliveryGroupStore {
    override fun find(code: String): RedeliveryGroup? {
        val hit = (cache.get(code) as RedeliveryGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryGroup> = inner.list()
}

fun newCachedRedeliveryGroupStore(inner: RedeliveryGroupStore, ttl: Int, clock: Clock): RedeliveryGroupStore {
    return CachedRedeliveryGroupStore(
        inner = inner,
        cache = newTtlCache<RedeliveryGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery groups. Empty fields match all. */
data class RedeliveryGroupQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryGroup>): MutableList<RedeliveryGroup> {
        val matched = filter(items) { candidate: RedeliveryGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryGroups(store: RedeliveryGroupStore, query: RedeliveryGroupQuery): MutableList<RedeliveryGroup> {
    return query.apply(store.list())
}
