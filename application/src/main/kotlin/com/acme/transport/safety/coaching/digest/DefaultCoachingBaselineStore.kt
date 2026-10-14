package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default coaching baselines by code. */
interface DefaultCoachingBaselineStore {
    fun find(code: String): DefaultCoachingBaseline?
    fun save(item: DefaultCoachingBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCoachingBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCoachingBaselineStore(
    var items: MutableMap<String, DefaultCoachingBaseline>,
    var order: MutableList<String>,
) : DefaultCoachingBaselineStore {
    override fun find(code: String): DefaultCoachingBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCoachingBaseline) {
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

    override fun list(): MutableList<DefaultCoachingBaseline> {
        val out = mutableListOf<DefaultCoachingBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCoachingBaselineStore(): DefaultCoachingBaselineStore {
    return MemoryDefaultCoachingBaselineStore(
        mutableMapOf<String, DefaultCoachingBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCoachingBaselineStore(
    var inner: DefaultCoachingBaselineStore,
    var cache: TtlCache<DefaultCoachingBaseline>,
    var hits: Int,
    var misses: Int,
) : DefaultCoachingBaselineStore {
    override fun find(code: String): DefaultCoachingBaseline? {
        val hit = (cache.get(code) as DefaultCoachingBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCoachingBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCoachingBaseline> = inner.list()
}

fun newCachedDefaultCoachingBaselineStore(inner: DefaultCoachingBaselineStore, ttl: Int, clock: Clock): DefaultCoachingBaselineStore {
    return CachedDefaultCoachingBaselineStore(
        inner = inner,
        cache = newTtlCache<DefaultCoachingBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default coaching baselines. Empty fields match all. */
data class DefaultCoachingBaselineQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCoachingBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCoachingBaseline>): MutableList<DefaultCoachingBaseline> {
        val matched = filter(items) { candidate: DefaultCoachingBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCoachingBaselines(store: DefaultCoachingBaselineStore, query: DefaultCoachingBaselineQuery): MutableList<DefaultCoachingBaseline> {
    return query.apply(store.list())
}
