package com.acme.transport.safety.incident.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident baselines by code. */
interface IncidentBaselineStore {
    fun find(code: String): IncidentBaseline?
    fun save(item: IncidentBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentBaselineStore(
    var items: MutableMap<String, IncidentBaseline>,
    var order: MutableList<String>,
) : IncidentBaselineStore {
    override fun find(code: String): IncidentBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentBaseline) {
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

    override fun list(): MutableList<IncidentBaseline> {
        val out = mutableListOf<IncidentBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentBaselineStore(): IncidentBaselineStore {
    return MemoryIncidentBaselineStore(mutableMapOf<String, IncidentBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentBaselineStore(
    var inner: IncidentBaselineStore,
    var cache: TtlCache<IncidentBaseline>,
    var hits: Int,
    var misses: Int,
) : IncidentBaselineStore {
    override fun find(code: String): IncidentBaseline? {
        val hit = (cache.get(code) as IncidentBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentBaseline> = inner.list()
}

fun newCachedIncidentBaselineStore(inner: IncidentBaselineStore, ttl: Int, clock: Clock): IncidentBaselineStore {
    return CachedIncidentBaselineStore(
        inner = inner,
        cache = newTtlCache<IncidentBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident baselines. Empty fields match all. */
data class IncidentBaselineQuery(
    var category: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentBaseline): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentBaseline>): MutableList<IncidentBaseline> {
        val matched = filter(items) { candidate: IncidentBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentBaselines(store: IncidentBaselineStore, query: IncidentBaselineQuery): MutableList<IncidentBaseline> {
    return query.apply(store.list())
}
