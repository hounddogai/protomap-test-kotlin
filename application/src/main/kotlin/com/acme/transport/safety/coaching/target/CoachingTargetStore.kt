package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching targets by code. */
interface CoachingTargetStore {
    fun find(code: String): CoachingTarget?
    fun save(item: CoachingTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingTargetStore(
    var items: MutableMap<String, CoachingTarget>,
    var order: MutableList<String>,
) : CoachingTargetStore {
    override fun find(code: String): CoachingTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingTarget) {
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

    override fun list(): MutableList<CoachingTarget> {
        val out = mutableListOf<CoachingTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingTargetStore(): CoachingTargetStore {
    return MemoryCoachingTargetStore(mutableMapOf<String, CoachingTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingTargetStore(
    var inner: CoachingTargetStore,
    var cache: TtlCache<CoachingTarget>,
    var hits: Int,
    var misses: Int,
) : CoachingTargetStore {
    override fun find(code: String): CoachingTarget? {
        val hit = (cache.get(code) as CoachingTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingTarget> = inner.list()
}

fun newCachedCoachingTargetStore(inner: CoachingTargetStore, ttl: Int, clock: Clock): CoachingTargetStore {
    return CachedCoachingTargetStore(
        inner = inner,
        cache = newTtlCache<CoachingTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching targets. Empty fields match all. */
data class CoachingTargetQuery(
    var title: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingTarget>): MutableList<CoachingTarget> {
        val matched = filter(items) { candidate: CoachingTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingTargets(store: CoachingTargetStore, query: CoachingTargetQuery): MutableList<CoachingTarget> {
    return query.apply(store.list())
}
