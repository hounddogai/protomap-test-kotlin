package com.acme.transport.emissions.idling.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling groups by code. */
interface IdlingGroupStore {
    fun find(code: String): IdlingGroup?
    fun save(item: IdlingGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingGroupStore(
    var items: MutableMap<String, IdlingGroup>,
    var order: MutableList<String>,
) : IdlingGroupStore {
    override fun find(code: String): IdlingGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingGroup) {
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

    override fun list(): MutableList<IdlingGroup> {
        val out = mutableListOf<IdlingGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingGroupStore(): IdlingGroupStore {
    return MemoryIdlingGroupStore(mutableMapOf<String, IdlingGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingGroupStore(
    var inner: IdlingGroupStore,
    var cache: TtlCache<IdlingGroup>,
    var hits: Int,
    var misses: Int,
) : IdlingGroupStore {
    override fun find(code: String): IdlingGroup? {
        val hit = (cache.get(code) as IdlingGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingGroup> = inner.list()
}

fun newCachedIdlingGroupStore(inner: IdlingGroupStore, ttl: Int, clock: Clock): IdlingGroupStore {
    return CachedIdlingGroupStore(
        inner = inner,
        cache = newTtlCache<IdlingGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling groups. Empty fields match all. */
data class IdlingGroupQuery(
    var category: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingGroup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingGroup>): MutableList<IdlingGroup> {
        val matched = filter(items) { candidate: IdlingGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingGroups(store: IdlingGroupStore, query: IdlingGroupQuery): MutableList<IdlingGroup> {
    return query.apply(store.list())
}
