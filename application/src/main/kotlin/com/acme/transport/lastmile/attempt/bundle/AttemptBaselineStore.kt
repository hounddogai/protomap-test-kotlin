package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt baselines by code. */
interface AttemptBaselineStore {
    fun find(code: String): AttemptBaseline?
    fun save(item: AttemptBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptBaselineStore(
    var items: MutableMap<String, AttemptBaseline>,
    var order: MutableList<String>,
) : AttemptBaselineStore {
    override fun find(code: String): AttemptBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptBaseline) {
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

    override fun list(): MutableList<AttemptBaseline> {
        val out = mutableListOf<AttemptBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptBaselineStore(): AttemptBaselineStore {
    return MemoryAttemptBaselineStore(mutableMapOf<String, AttemptBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptBaselineStore(
    var inner: AttemptBaselineStore,
    var cache: TtlCache<AttemptBaseline>,
    var hits: Int,
    var misses: Int,
) : AttemptBaselineStore {
    override fun find(code: String): AttemptBaseline? {
        val hit = (cache.get(code) as AttemptBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptBaseline> = inner.list()
}

fun newCachedAttemptBaselineStore(inner: AttemptBaselineStore, ttl: Int, clock: Clock): AttemptBaselineStore {
    return CachedAttemptBaselineStore(
        inner = inner,
        cache = newTtlCache<AttemptBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt baselines. Empty fields match all. */
data class AttemptBaselineQuery(
    var depot: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptBaseline): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptBaseline>): MutableList<AttemptBaseline> {
        val matched = filter(items) { candidate: AttemptBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptBaselines(store: AttemptBaselineStore, query: AttemptBaselineQuery): MutableList<AttemptBaseline> {
    return query.apply(store.list())
}
