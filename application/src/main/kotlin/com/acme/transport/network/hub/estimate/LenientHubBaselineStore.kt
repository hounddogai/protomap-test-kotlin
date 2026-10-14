package com.acme.transport.network.hub.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient hub baselines by code. */
interface LenientHubBaselineStore {
    fun find(code: String): LenientHubBaseline?
    fun save(item: LenientHubBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientHubBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientHubBaselineStore(
    var items: MutableMap<String, LenientHubBaseline>,
    var order: MutableList<String>,
) : LenientHubBaselineStore {
    override fun find(code: String): LenientHubBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientHubBaseline) {
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

    override fun list(): MutableList<LenientHubBaseline> {
        val out = mutableListOf<LenientHubBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientHubBaselineStore(): LenientHubBaselineStore {
    return MemoryLenientHubBaselineStore(mutableMapOf<String, LenientHubBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientHubBaselineStore(
    var inner: LenientHubBaselineStore,
    var cache: TtlCache<LenientHubBaseline>,
    var hits: Int,
    var misses: Int,
) : LenientHubBaselineStore {
    override fun find(code: String): LenientHubBaseline? {
        val hit = (cache.get(code) as LenientHubBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientHubBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientHubBaseline> = inner.list()
}

fun newCachedLenientHubBaselineStore(inner: LenientHubBaselineStore, ttl: Int, clock: Clock): LenientHubBaselineStore {
    return CachedLenientHubBaselineStore(
        inner = inner,
        cache = newTtlCache<LenientHubBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient hub baselines. Empty fields match all. */
data class LenientHubBaselineQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientHubBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientHubBaseline>): MutableList<LenientHubBaseline> {
        val matched = filter(items) { candidate: LenientHubBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientHubBaselines(store: LenientHubBaselineStore, query: LenientHubBaselineQuery): MutableList<LenientHubBaseline> {
    return query.apply(store.list())
}
