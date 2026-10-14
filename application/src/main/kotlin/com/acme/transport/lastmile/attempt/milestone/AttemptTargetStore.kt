package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt targets by code. */
interface AttemptTargetStore {
    fun find(code: String): AttemptTarget?
    fun save(item: AttemptTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptTargetStore(
    var items: MutableMap<String, AttemptTarget>,
    var order: MutableList<String>,
) : AttemptTargetStore {
    override fun find(code: String): AttemptTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptTarget) {
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

    override fun list(): MutableList<AttemptTarget> {
        val out = mutableListOf<AttemptTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptTargetStore(): AttemptTargetStore {
    return MemoryAttemptTargetStore(mutableMapOf<String, AttemptTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptTargetStore(
    var inner: AttemptTargetStore,
    var cache: TtlCache<AttemptTarget>,
    var hits: Int,
    var misses: Int,
) : AttemptTargetStore {
    override fun find(code: String): AttemptTarget? {
        val hit = (cache.get(code) as AttemptTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptTarget> = inner.list()
}

fun newCachedAttemptTargetStore(inner: AttemptTargetStore, ttl: Int, clock: Clock): AttemptTargetStore {
    return CachedAttemptTargetStore(
        inner = inner,
        cache = newTtlCache<AttemptTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt targets. Empty fields match all. */
data class AttemptTargetQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<AttemptTarget>): MutableList<AttemptTarget> {
        val matched = filter(items) { candidate: AttemptTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptTargets(store: AttemptTargetStore, query: AttemptTargetQuery): MutableList<AttemptTarget> {
    return query.apply(store.list())
}
