package com.acme.transport.network.cutoff.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff baselines by code. */
interface CutoffBaselineStore {
    fun find(code: String): CutoffBaseline?
    fun save(item: CutoffBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffBaselineStore(
    var items: MutableMap<String, CutoffBaseline>,
    var order: MutableList<String>,
) : CutoffBaselineStore {
    override fun find(code: String): CutoffBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffBaseline) {
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

    override fun list(): MutableList<CutoffBaseline> {
        val out = mutableListOf<CutoffBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffBaselineStore(): CutoffBaselineStore {
    return MemoryCutoffBaselineStore(mutableMapOf<String, CutoffBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffBaselineStore(
    var inner: CutoffBaselineStore,
    var cache: TtlCache<CutoffBaseline>,
    var hits: Int,
    var misses: Int,
) : CutoffBaselineStore {
    override fun find(code: String): CutoffBaseline? {
        val hit = (cache.get(code) as CutoffBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffBaseline> = inner.list()
}

fun newCachedCutoffBaselineStore(inner: CutoffBaselineStore, ttl: Int, clock: Clock): CutoffBaselineStore {
    return CachedCutoffBaselineStore(
        inner = inner,
        cache = newTtlCache<CutoffBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff baselines. Empty fields match all. */
data class CutoffBaselineQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffBaseline>): MutableList<CutoffBaseline> {
        val matched = filter(items) { candidate: CutoffBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffBaselines(store: CutoffBaselineStore, query: CutoffBaselineQuery): MutableList<CutoffBaseline> {
    return query.apply(store.list())
}
