package com.acme.transport.safety.nearmiss.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss baselines by code. */
interface NearMissBaselineStore {
    fun find(code: String): NearMissBaseline?
    fun save(item: NearMissBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissBaselineStore(
    var items: MutableMap<String, NearMissBaseline>,
    var order: MutableList<String>,
) : NearMissBaselineStore {
    override fun find(code: String): NearMissBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissBaseline) {
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

    override fun list(): MutableList<NearMissBaseline> {
        val out = mutableListOf<NearMissBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissBaselineStore(): NearMissBaselineStore {
    return MemoryNearMissBaselineStore(mutableMapOf<String, NearMissBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissBaselineStore(
    var inner: NearMissBaselineStore,
    var cache: TtlCache<NearMissBaseline>,
    var hits: Int,
    var misses: Int,
) : NearMissBaselineStore {
    override fun find(code: String): NearMissBaseline? {
        val hit = (cache.get(code) as NearMissBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissBaseline> = inner.list()
}

fun newCachedNearMissBaselineStore(inner: NearMissBaselineStore, ttl: Int, clock: Clock): NearMissBaselineStore {
    return CachedNearMissBaselineStore(
        inner = inner,
        cache = newTtlCache<NearMissBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss baselines. Empty fields match all. */
data class NearMissBaselineQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissBaseline>): MutableList<NearMissBaseline> {
        val matched = filter(items) { candidate: NearMissBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissBaselines(store: NearMissBaselineStore, query: NearMissBaselineQuery): MutableList<NearMissBaseline> {
    return query.apply(store.list())
}
