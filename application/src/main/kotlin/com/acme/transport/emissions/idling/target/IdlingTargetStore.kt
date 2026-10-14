package com.acme.transport.emissions.idling.target

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling targets by code. */
interface IdlingTargetStore {
    fun find(code: String): IdlingTarget?
    fun save(item: IdlingTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingTargetStore(
    var items: MutableMap<String, IdlingTarget>,
    var order: MutableList<String>,
) : IdlingTargetStore {
    override fun find(code: String): IdlingTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingTarget) {
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

    override fun list(): MutableList<IdlingTarget> {
        val out = mutableListOf<IdlingTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingTargetStore(): IdlingTargetStore {
    return MemoryIdlingTargetStore(mutableMapOf<String, IdlingTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingTargetStore(
    var inner: IdlingTargetStore,
    var cache: TtlCache<IdlingTarget>,
    var hits: Int,
    var misses: Int,
) : IdlingTargetStore {
    override fun find(code: String): IdlingTarget? {
        val hit = (cache.get(code) as IdlingTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingTarget> = inner.list()
}

fun newCachedIdlingTargetStore(inner: IdlingTargetStore, ttl: Int, clock: Clock): IdlingTargetStore {
    return CachedIdlingTargetStore(
        inner = inner,
        cache = newTtlCache<IdlingTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling targets. Empty fields match all. */
data class IdlingTargetQuery(
    var lane: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingTarget): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingTarget>): MutableList<IdlingTarget> {
        val matched = filter(items) { candidate: IdlingTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingTargets(store: IdlingTargetStore, query: IdlingTargetQuery): MutableList<IdlingTarget> {
    return query.apply(store.list())
}
