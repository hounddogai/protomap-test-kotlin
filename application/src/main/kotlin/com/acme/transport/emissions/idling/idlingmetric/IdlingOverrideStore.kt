package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling overrides by code. */
interface IdlingOverrideStore {
    fun find(code: String): IdlingOverride?
    fun save(item: IdlingOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingOverrideStore(
    var items: MutableMap<String, IdlingOverride>,
    var order: MutableList<String>,
) : IdlingOverrideStore {
    override fun find(code: String): IdlingOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingOverride) {
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

    override fun list(): MutableList<IdlingOverride> {
        val out = mutableListOf<IdlingOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingOverrideStore(): IdlingOverrideStore {
    return MemoryIdlingOverrideStore(mutableMapOf<String, IdlingOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingOverrideStore(
    var inner: IdlingOverrideStore,
    var cache: TtlCache<IdlingOverride>,
    var hits: Int,
    var misses: Int,
) : IdlingOverrideStore {
    override fun find(code: String): IdlingOverride? {
        val hit = (cache.get(code) as IdlingOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingOverride> = inner.list()
}

fun newCachedIdlingOverrideStore(inner: IdlingOverrideStore, ttl: Int, clock: Clock): IdlingOverrideStore {
    return CachedIdlingOverrideStore(
        inner = inner,
        cache = newTtlCache<IdlingOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling overrides. Empty fields match all. */
data class IdlingOverrideQuery(
    var segment: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingOverride): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<IdlingOverride>): MutableList<IdlingOverride> {
        val matched = filter(items) { candidate: IdlingOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingOverrides(store: IdlingOverrideStore, query: IdlingOverrideQuery): MutableList<IdlingOverride> {
    return query.apply(store.list())
}
