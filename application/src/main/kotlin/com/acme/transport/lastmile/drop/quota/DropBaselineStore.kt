package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop baselines by code. */
interface DropBaselineStore {
    fun find(code: String): DropBaseline?
    fun save(item: DropBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropBaselineStore(
    var items: MutableMap<String, DropBaseline>,
    var order: MutableList<String>,
) : DropBaselineStore {
    override fun find(code: String): DropBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropBaseline) {
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

    override fun list(): MutableList<DropBaseline> {
        val out = mutableListOf<DropBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropBaselineStore(): DropBaselineStore {
    return MemoryDropBaselineStore(mutableMapOf<String, DropBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropBaselineStore(
    var inner: DropBaselineStore,
    var cache: TtlCache<DropBaseline>,
    var hits: Int,
    var misses: Int,
) : DropBaselineStore {
    override fun find(code: String): DropBaseline? {
        val hit = (cache.get(code) as DropBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropBaseline> = inner.list()
}

fun newCachedDropBaselineStore(inner: DropBaselineStore, ttl: Int, clock: Clock): DropBaselineStore {
    return CachedDropBaselineStore(
        inner = inner,
        cache = newTtlCache<DropBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop baselines. Empty fields match all. */
data class DropBaselineQuery(
    var segment: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropBaseline): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropBaseline>): MutableList<DropBaseline> {
        val matched = filter(items) { candidate: DropBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropBaselines(store: DropBaselineStore, query: DropBaselineQuery): MutableList<DropBaseline> {
    return query.apply(store.list())
}
