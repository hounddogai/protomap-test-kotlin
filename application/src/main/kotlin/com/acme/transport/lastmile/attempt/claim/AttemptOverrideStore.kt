package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt overrides by code. */
interface AttemptOverrideStore {
    fun find(code: String): AttemptOverride?
    fun save(item: AttemptOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptOverrideStore(
    var items: MutableMap<String, AttemptOverride>,
    var order: MutableList<String>,
) : AttemptOverrideStore {
    override fun find(code: String): AttemptOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptOverride) {
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

    override fun list(): MutableList<AttemptOverride> {
        val out = mutableListOf<AttemptOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptOverrideStore(): AttemptOverrideStore {
    return MemoryAttemptOverrideStore(mutableMapOf<String, AttemptOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptOverrideStore(
    var inner: AttemptOverrideStore,
    var cache: TtlCache<AttemptOverride>,
    var hits: Int,
    var misses: Int,
) : AttemptOverrideStore {
    override fun find(code: String): AttemptOverride? {
        val hit = (cache.get(code) as AttemptOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptOverride> = inner.list()
}

fun newCachedAttemptOverrideStore(inner: AttemptOverrideStore, ttl: Int, clock: Clock): AttemptOverrideStore {
    return CachedAttemptOverrideStore(
        inner = inner,
        cache = newTtlCache<AttemptOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt overrides. Empty fields match all. */
data class AttemptOverrideQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptOverride): Boolean {
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

    fun apply(items: MutableList<AttemptOverride>): MutableList<AttemptOverride> {
        val matched = filter(items) { candidate: AttemptOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptOverrides(store: AttemptOverrideStore, query: AttemptOverrideQuery): MutableList<AttemptOverride> {
    return query.apply(store.list())
}
