package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching bundles by code. */
interface CoachingBundleStore {
    fun find(code: String): CoachingBundle?
    fun save(item: CoachingBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingBundleStore(
    var items: MutableMap<String, CoachingBundle>,
    var order: MutableList<String>,
) : CoachingBundleStore {
    override fun find(code: String): CoachingBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingBundle) {
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

    override fun list(): MutableList<CoachingBundle> {
        val out = mutableListOf<CoachingBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingBundleStore(): CoachingBundleStore {
    return MemoryCoachingBundleStore(mutableMapOf<String, CoachingBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingBundleStore(
    var inner: CoachingBundleStore,
    var cache: TtlCache<CoachingBundle>,
    var hits: Int,
    var misses: Int,
) : CoachingBundleStore {
    override fun find(code: String): CoachingBundle? {
        val hit = (cache.get(code) as CoachingBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingBundle> = inner.list()
}

fun newCachedCoachingBundleStore(inner: CoachingBundleStore, ttl: Int, clock: Clock): CoachingBundleStore {
    return CachedCoachingBundleStore(
        inner = inner,
        cache = newTtlCache<CoachingBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching bundles. Empty fields match all. */
data class CoachingBundleQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingBundle>): MutableList<CoachingBundle> {
        val matched = filter(items) { candidate: CoachingBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingBundles(store: CoachingBundleStore, query: CoachingBundleQuery): MutableList<CoachingBundle> {
    return query.apply(store.list())
}
