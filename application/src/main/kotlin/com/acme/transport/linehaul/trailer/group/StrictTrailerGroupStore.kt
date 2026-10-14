package com.acme.transport.linehaul.trailer.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict trailer groups by code. */
interface StrictTrailerGroupStore {
    fun find(code: String): StrictTrailerGroup?
    fun save(item: StrictTrailerGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTrailerGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTrailerGroupStore(
    var items: MutableMap<String, StrictTrailerGroup>,
    var order: MutableList<String>,
) : StrictTrailerGroupStore {
    override fun find(code: String): StrictTrailerGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTrailerGroup) {
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

    override fun list(): MutableList<StrictTrailerGroup> {
        val out = mutableListOf<StrictTrailerGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTrailerGroupStore(): StrictTrailerGroupStore {
    return MemoryStrictTrailerGroupStore(mutableMapOf<String, StrictTrailerGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTrailerGroupStore(
    var inner: StrictTrailerGroupStore,
    var cache: TtlCache<StrictTrailerGroup>,
    var hits: Int,
    var misses: Int,
) : StrictTrailerGroupStore {
    override fun find(code: String): StrictTrailerGroup? {
        val hit = (cache.get(code) as StrictTrailerGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTrailerGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTrailerGroup> = inner.list()
}

fun newCachedStrictTrailerGroupStore(inner: StrictTrailerGroupStore, ttl: Int, clock: Clock): StrictTrailerGroupStore {
    return CachedStrictTrailerGroupStore(
        inner = inner,
        cache = newTtlCache<StrictTrailerGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict trailer groups. Empty fields match all. */
data class StrictTrailerGroupQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTrailerGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTrailerGroup>): MutableList<StrictTrailerGroup> {
        val matched = filter(items) { candidate: StrictTrailerGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTrailerGroups(store: StrictTrailerGroupStore, query: StrictTrailerGroupQuery): MutableList<StrictTrailerGroup> {
    return query.apply(store.list())
}
